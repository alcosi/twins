package org.twins.core.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

/**
 * Sets the {@code app.current_user_id} GUC (SET LOCAL, transaction-scoped) so the versioning
 * triggers can write {@code domain_version_audit.actor_user_id} via
 * {@code current_actor_user_id_get()} (TWINS-719). The GUC dies with the transaction —
 * no connection-pool leakage.
 * <p>
 * Called from the {@code TwinsEntitySecureFindService} write family (save/update/deleteSafe).
 * One {@code set_config} roundtrip per transaction: the thread-local marker skips repeats and is
 * cleared by synchronization hooks — including suspend/resume, so a REQUIRES_NEW inner
 * transaction (different connection) re-sets the GUC instead of inheriting a stale marker.
 * Without an auth context (system jobs) the call is a no-op and the trigger writes NULL actor.
 */
@Service
@RequiredArgsConstructor
public class TransactionActorUserService {
    private static final ThreadLocal<UUID> FILLED = new ThreadLocal<>();

    @PersistenceContext
    private final EntityManager entityManager;
    private final AuthService authService;

    public void fillCurrentActorUser() {
        UUID userId;
        try {
            userId = authService.getApiUser().getUserId();
        } catch (ServiceException e) {
            // system path without an actor — leave the GUC unset, audit row gets NULL actor
            return;
        }
        if (userId == null || userId.equals(FILLED.get()))
            return;
        entityManager.createNativeQuery("select set_config('app.current_user_id', ?1, true)")
                .setParameter(1, userId.toString())
                .getSingleResult();
        FILLED.set(userId);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void suspend() {
                    FILLED.remove();
                }

                @Override
                public void resume() {
                    FILLED.remove();
                }

                @Override
                public void afterCompletion(int status) {
                    FILLED.remove();
                }
            });
        } else {
            // no Spring transaction — the SET LOCAL evaporates with the statement's own tx
            FILLED.remove();
        }
    }
}
