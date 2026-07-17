package org.twins.core.featurer.pointer;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.LTreeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.twin.TwinService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

class PointerOnHeadTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private PointerOnHead pointer;

    @BeforeEach
    void setUp() {
        pointer = new PointerOnHead(twinService);
    }

    private void seedHead(TwinEntity src, TwinEntity head) throws ServiceException {
        doAnswer(invocation -> {
            Collection<TwinEntity> twins = invocation.getArgument(0);
            for (TwinEntity t : twins) {
                if (t == src) {
                    t.setHeadTwin(head);
                }
            }
            return null;
        }).when(twinService).loadHead(anyCollection());
    }

    @Nested
    class Load {

        @Test
        void load_depthOne_loadsImmediateHead() throws ServiceException {
            var headTwin = new TwinEntity();
            var srcTwin = new TwinEntity().setId(UUID.randomUUID());
            srcTwin.setHeadTwinId(UUID.randomUUID());
            seedHead(srcTwin, headTwin);

            Map<UUID, TwinEntity> result = pointer.load(new Properties(), List.of(srcTwin));

            assertSame(headTwin, result.get(srcTwin.getId()));
        }

        @Test
        void load_depthTwo_walksLoadedChainWithoutDbQuery() throws ServiceException {
            // the whole chain is already in memory -> resolve from it, no loadHead, no findEntitiesSafe
            var grandparent = new TwinEntity().setId(UUID.randomUUID());
            var head1 = new TwinEntity().setId(UUID.randomUUID());
            var src = new TwinEntity().setId(UUID.randomUUID());
            src.setHeadTwin(head1);
            head1.setHeadTwin(grandparent);

            var props = new Properties();
            props.put("depth", "2");

            Map<UUID, TwinEntity> result = pointer.load(props, List.of(src));

            assertSame(grandparent, result.get(src.getId()));
            verify(twinService, never()).loadHead(anyCollection());
            verify(twinService, never()).findEntitiesSafe(anyCollection());
        }

        @Test
        void load_depthTwo_fallsBackToHierarchyWhenChainNotLoaded() throws ServiceException {
            // head chain not loaded (src.headTwin == null) -> jump via hierarchy tree, load only the target
            var grandparent = new TwinEntity().setId(UUID.randomUUID());
            var head1 = new TwinEntity().setId(UUID.randomUUID());
            var src = new TwinEntity().setId(UUID.randomUUID());
            src.setHierarchyTree(LTreeUtils.convertToChainLTreeFormat(grandparent.getId(), head1.getId(), src.getId()));
            Kit<TwinEntity, UUID> loaded = new Kit<>(TwinEntity::getId);
            loaded.add(grandparent);
            doReturn(loaded).when(twinService).findEntitiesSafe(anyCollection());

            var props = new Properties();
            props.put("depth", "2");

            Map<UUID, TwinEntity> result = pointer.load(props, List.of(src));

            assertSame(grandparent, result.get(src.getId()));
            verify(twinService, never()).loadHead(anyCollection());
        }

        @Test
        void load_depthBeyondHierarchy_resolvesToNull() throws ServiceException {
            var head1 = new TwinEntity().setId(UUID.randomUUID());
            var src = new TwinEntity().setId(UUID.randomUUID());
            src.setHierarchyTree(LTreeUtils.convertToChainLTreeFormat(head1.getId(), src.getId()));

            var props = new Properties();
            props.put("depth", "5");

            Map<UUID, TwinEntity> result = pointer.load(props, List.of(src));

            assertNull(result.get(src.getId()));
        }

        @Test
        void load_srcWithoutHierarchy_resolvesToNull() throws ServiceException {
            var src = new TwinEntity().setId(UUID.randomUUID());

            var props = new Properties();
            props.put("depth", "2");

            Map<UUID, TwinEntity> result = pointer.load(props, List.of(src));

            assertNull(result.get(src.getId()));
        }

        @Test
        void load_srcIsRoot_depthOne_resolvesToNull() throws ServiceException {
            var src = new TwinEntity().setId(UUID.randomUUID());
            doAnswer(invocation -> null).when(twinService).loadHead(anyCollection());

            Map<UUID, TwinEntity> result = pointer.load(new Properties(), List.of(src));

            assertNull(result.get(src.getId()));
        }
    }
}
