package org.twins.core.integration.service.twin;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinChangesApplyResult;
import org.twins.core.domain.TwinChangesCollector;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Same head-chain FK-ordering scenario as {@link TwinChangesHeadFkOrderingIntegrationTest}, but forced
 * under {@code hibernate.order_inserts=true} — the setting used by every non-default profile
 * (dev/localhost/stage/onsdev2/onstest), whereas the default {@code application.properties} keeps
 * {@code order_inserts=false}. With ordering enabled, Hibernate regroups the insert action queue, so the
 * fix must not silently rely on insert order equaling list order. This test fails if a future Hibernate
 * upgrade or config change breaks that assumption for same-entity-type batches.
 */
@TestPropertySource(properties = {
        "hibernate.order_inserts=true",
        "hibernate.order_updates=true"
})
public class TwinChangesHeadFkOrderingOrderInsertsIntegrationTest extends AbstractTwinHeadFkOrderingTest {

    @Test
    public void applyChanges_headChainInArbitraryOrder_persistsAllWithoutFkViolation_orderInsertsEnabled() throws ServiceException {
        int chainLength = 20;
        UUID[] ids = new UUID[chainLength];
        for (int i = 0; i < chainLength; i++) {
            ids[i] = UUID.randomUUID();
        }
        TwinEntity[] chain = new TwinEntity[chainLength];
        buildHeadChain(chainLength, ids, chain);

        TwinChangesCollector collector = new TwinChangesCollector();
        for (int i = 0; i < chainLength; i++) {
            collector.add(chain[i]);
        }

        TwinChangesApplyResult result = twinChangesService.applyChanges(collector); // must not throw

        assertChainPersistedHeadFirst(ids, chain, chainLength);
        assertNotNull(result);
    }
}
