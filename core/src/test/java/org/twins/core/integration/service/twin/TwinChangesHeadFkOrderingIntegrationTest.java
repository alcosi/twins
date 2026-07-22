package org.twins.core.integration.service.twin;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinChangesApplyResult;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.twin.TwinHeadService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Reproduces the head_twin_id FK ordering bug: when a batch of new twins reference each other via
 * head_twin_id and are flushed in arbitrary order, the non-deferrable twin_head_twin_id_fk can be
 * violated because a child is INSERTed before its head parent.
 *
 * <p>With the fix, {@link org.twins.core.service.TwinChangesService} sorts the batch by hierarchyTree
 * depth (head-first) so a parent is always persisted before its children, and rejects (with
 * {@link ErrorCodeTwins#ENTITY_INVALID}) any twin whose hierarchyTree was not populated — the case that
 * would otherwise defeat the sort.
 *
 * <p>This class runs under the default test Hibernate config. See
 * {@link TwinChangesHeadFkOrderingOrderInsertsIntegrationTest} for the same scenario under
 * {@code hibernate.order_inserts=true} (the prod-profile setting).
 */
public class TwinChangesHeadFkOrderingIntegrationTest extends AbstractTwinHeadFkOrderingTest {

    /**
     * A single deep chain (length 20) is used because there is exactly one correct head-first persistence
     * order; without the sort the probability of a random iteration matching it is ~1/20!, so the FK
     * violation is effectively deterministic.
     */
    @Test
    public void applyChanges_headChainInArbitraryOrder_persistsAllWithoutFkViolation() throws ServiceException {
        int chainLength = 20;
        UUID[] ids = new UUID[chainLength];
        for (int i = 0; i < chainLength; i++) {
            ids[i] = UUID.randomUUID();
        }
        TwinEntity[] chain = new TwinEntity[chainLength];
        buildHeadChain(chainLength, ids, chain);

        TwinChangesCollector collector = new TwinChangesCollector();
        // Add child-first into the collector; the collector's ConcurrentHashMap iterates in
        // non-deterministic order, which is precisely why the depth sort is required.
        for (int i = 0; i < chainLength; i++) {
            collector.add(chain[i]);
        }

        TwinChangesApplyResult result = twinChangesService.applyChanges(collector); // must not throw

        assertChainPersistedHeadFirst(ids, chain, chainLength);
        assertNotNull(result);
    }

    /**
     * Guards the fix against itself: a twin that carries a headTwinId but no hierarchyTree would sort to
     * depth 0 (first), ahead of its still-unsaved parent, and re-trigger the FK violation the sort
     * exists to prevent. The save path must fail fast on this inconsistent state rather than silently
     * emit an unsortable batch.
     */
    @Test
    public void applyChanges_twinWithHeadButNoHierarchyTree_failsFast() {
        UUID rootId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();
        TwinEntity root = baseTwin(rootId, "root");
        TwinHeadService.initRootHierarchy(root);
        TwinEntity child = baseTwin(childId, "child");
        child.setHeadTwinId(rootId); // head referenced, but hierarchyTree deliberately left null

        TwinChangesCollector collector = new TwinChangesCollector();
        collector.add(child);
        collector.add(root);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> twinChangesService.applyChanges(collector));
        assertNotNull(ex);
    }
}
