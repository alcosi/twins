package org.twins.core.featurer.notificator.recipient;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.businessaccount.BusinessAccountUserService;

import java.util.*;

/**
 * Resolves all users within the business account as notification recipients.
 * <p>Result is a query result (not a relation), so this resolver overrides {@link #resolveBatch}
 * directly (no {@link RecipientResolverAtomic}): it collects the chunk's business account ids, runs
 * ONE bulk query via {@code findUserIdsByBusinessAccountIdIn}, and distributes the userIds per
 * history. The preload map is a local variable — thread-safe on the singleton bean.
 */
@Component
@Featurer(id = FeaturerTwins.ID_4706,
        name = "Business Account Recipient Resolver",
        description = "Resolves all users within the business account as notification recipients.")
public class RecipientResolverBusinessAccount extends RecipientResolver {

    @Autowired
    private BusinessAccountUserService businessAccountUserService;

    @Override
    public void resolveBatch(Map<HistoryEntity, Set<UUID>> recipientIdsByHistory, Properties properties) throws ServiceException {
        // collect distinct business account ids across the whole batch
        Set<UUID> businessAccountIds = new HashSet<>();
        for (HistoryEntity history : recipientIdsByHistory.keySet()) {
            UUID businessAccountId = history.getTwin().getOwnerBusinessAccountId();
            if (businessAccountId != null) {
                businessAccountIds.add(businessAccountId);
            }
        }
        if (businessAccountIds.isEmpty()) {
            return;
        }
        // one bulk query → Map<businessAccountId, Set<userId>>
        Map<UUID, Set<UUID>> userIdsByBusinessAccount = businessAccountUserService.findUserIdsByBusinessAccountIdIn(businessAccountIds);
        // distribute per history
        for (Map.Entry<HistoryEntity, Set<UUID>> entry : recipientIdsByHistory.entrySet()) {
            UUID businessAccountId = entry.getKey().getTwin().getOwnerBusinessAccountId();
            Set<UUID> userIds = businessAccountId == null ? null : userIdsByBusinessAccount.get(businessAccountId);
            if (CollectionUtils.isNotEmpty(userIds)) {
                entry.getValue().addAll(userIds);
            }
        }
    }
}
