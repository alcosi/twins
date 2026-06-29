package org.twins.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Triggers {@link GlossaryBootstrapService#bootstrap()} once the Spring context is fully ready.
 *
 * <p>Controlled by {@code twins.glossary.bootstrap.enabled} (default {@code true}). Disabled in
 * tests via {@code @TestPropertySource} or {@code application-test.properties}.</p>
 *
 * <p>Catches all exceptions — bootstrap failure MUST NOT abort application startup. Glossary
 * Twins may be in pre-bootstrap state until next restart with a fix.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Order(20)  // must run AFTER SystemEntityBootstrapRunner — needs TWINS_GLOSSARY class already in DB
@ConditionalOnProperty(
        prefix = "twins.glossary.bootstrap",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class GlossaryBootstrapRunner implements ApplicationListener<ApplicationReadyEvent> {

    private final GlossaryBootstrapService bootstrapService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            GlossaryBootstrapResult result = bootstrapService.bootstrap();
            log.info("Glossary bootstrap result: created={}, updated={}, skipped={}, restored={}, markedDeleted={}, invalidFiles={}",
                    result.created(),
                    result.updated(),
                    result.skipped(),
                    result.orphansRestored(),
                    result.orphansMarkedDeleted(),
                    result.invalidFiles().size());
        } catch (Exception e) {
            log.error("Glossary bootstrap failed — glossary may be incomplete until next restart", e);
        }
    }
}
