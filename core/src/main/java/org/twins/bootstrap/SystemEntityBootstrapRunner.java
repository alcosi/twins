package org.twins.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Triggers {@link SystemEntityBootstrapService#bootstrap()} once the Spring context is fully ready.
 *
 * <p>Uses {@link ApplicationReadyEvent} (not {@link jakarta.annotation.PostConstruct}) so that
 * bootstrap runs even when {@code spring.main.lazy-initialization=true} — PostConstruct on a
 * lazy bean is never called unless the bean is injected somewhere, which would silently skip
 * system entity creation in dev profiles.</p>
 *
 * <p>Controlled by {@code twins.system-entity.bootstrap.enabled} (default {@code true}).</p>
 *
 * <p>Catches all exceptions — bootstrap failure MUST NOT abort application startup. The system
 * may end up in a partial state, but that is preferable to a startup crash; subsequent restarts
 * will retry (operations are idempotent via {@code SaveMode.ifNotPresentCreate}).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Order(10)  // must run BEFORE GlossaryBootstrapRunner — glossary needs TWINS_GLOSSARY class in DB
@ConditionalOnProperty(
        prefix = "twins.system-entity.bootstrap",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class SystemEntityBootstrapRunner implements ApplicationListener<ApplicationReadyEvent> {

    private final SystemEntityBootstrapService bootstrapService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            bootstrapService.bootstrap();
            log.info("System entity bootstrap completed");
        } catch (ServiceException e) {
            log.error("System entity bootstrap failed — system entities may be incomplete until next restart", e);
        }
    }
}
