package org.twins.core.featurer.pointer;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.service.twin.TwinPointerService;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PointerOnPointerChainedTest extends BaseUnitTest {

    @Mock
    private TwinPointerService twinPointerService;
    @Mock
    private FeaturerService featurerService;
    @Mock
    private Pointer subFeaturer;

    private PointerOnPointerChained pointer;

    @BeforeEach
    void setUp() throws ServiceException {
        pointer = new PointerOnPointerChained(twinPointerService);
        pointer.featurerService = featurerService; // public field on the Featurer base class
        // by default every sub-pointer resolves through the shared subFeaturer mock.
        // lenient: a few tests throw before reaching a sub-pointer or re-stub this themselves.
        lenient().doReturn(subFeaturer).when(featurerService).getFeaturer(anyInt(), eq(Pointer.class));
    }

    private Properties props(String csv) {
        var p = new Properties();
        p.put("pointerIds", csv);
        return p;
    }

    private TwinPointerEntity pointerEntity(UUID id, int featurerId) {
        return new TwinPointerEntity().setId(id).setPointerFeaturerId(featurerId);
    }

    private void stubSubPointer(UUID subPointerId, int featurerId) throws ServiceException {
        doReturn(pointerEntity(subPointerId, featurerId)).when(twinPointerService).findEntitySafe(subPointerId);
    }

    /** Stubs the sub-pointer load: for every current twin, look up its resolved target and cache it. */
    private void stubResolve(Map<TwinEntity, TwinEntity> resolveTo) throws ServiceException {
        doAnswer(invocation -> {
            TwinPointerEntity sub = invocation.getArgument(0);
            Collection<TwinEntity> twins = invocation.getArgument(1);
            for (TwinEntity t : twins) {
                TwinEntity target = resolveTo.get(t);
                if (target != null) {
                    t.addPointer(sub.getId(), target);
                }
            }
            return null;
        }).when(subFeaturer).load(org.mockito.ArgumentMatchers.any(TwinPointerEntity.class), anyCollection());
    }

    @Nested
    class Load {

        @Test
        void emptyChain_returnsEmpty() throws ServiceException {
            var src = new TwinEntity().setId(UUID.randomUUID());

            Map<UUID, TwinEntity> result = pointer.load(props(""), java.util.List.of(src));

            assertTrue(result.isEmpty());
        }

        @Test
        void singleHop_resolvesViaSubPointer() throws ServiceException {
            var target = new TwinEntity().setId(UUID.randomUUID());
            var src = new TwinEntity().setId(UUID.randomUUID());
            var p1 = UUID.randomUUID();
            stubSubPointer(p1, 3103);
            stubResolve(Map.of(src, target));

            Map<UUID, TwinEntity> result = pointer.load(props(p1.toString()), java.util.List.of(src));

            assertSame(target, result.get(src.getId()));
        }

        @Test
        void twoHops_chainsThrough() throws ServiceException {
            var finalTwin = new TwinEntity().setId(UUID.randomUUID());
            var mid = new TwinEntity().setId(UUID.randomUUID());
            var src = new TwinEntity().setId(UUID.randomUUID());
            var p1 = UUID.randomUUID();
            var p2 = UUID.randomUUID();
            stubSubPointer(p1, 3103);
            stubSubPointer(p2, 3102);
            stubResolve(Map.of(src, mid, mid, finalTwin));

            Map<UUID, TwinEntity> result = pointer.load(props(p1 + "," + p2), java.util.List.of(src));

            assertSame(finalTwin, result.get(src.getId()));
        }

        @Test
        void subPointerIsPointerChained_throwsCycleGuard() throws ServiceException {
            var src = new TwinEntity().setId(UUID.randomUUID());
            var p1 = UUID.randomUUID();
            stubSubPointer(p1, 3110);
            // a sub-pointer that is itself a PointerOnPointerChained -> the only possible cycle vector
            Pointer nested = mock(PointerOnPointerChained.class);
            doReturn(nested).when(featurerService).getFeaturer(anyInt(), eq(Pointer.class));

            var ex = assertThrows(ServiceException.class,
                    () -> pointer.load(props(p1.toString()), java.util.List.of(src)));
            assertEquals(ErrorCodeCommon.FEATURER_WRONG_PARAMS.getCode(), ex.getErrorCode());
        }

        @Test
        void chainTooDeep_throws() {
            var sb = new StringBuilder();
            for (int i = 0; i <= PointerOnPointerChained.MAX_DEPTH; i++) { // MAX_DEPTH + 1 ids
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(UUID.randomUUID());
            }
            var src = new TwinEntity().setId(UUID.randomUUID());

            var ex = assertThrows(ServiceException.class, () -> pointer.load(props(sb.toString()), java.util.List.of(src)));
            assertEquals(ErrorCodeCommon.FEATURER_WRONG_PARAMS.getCode(), ex.getErrorCode());
        }

        @Test
        void unresolvedIntermediate_dropsSourceFromResult() throws ServiceException {
            // p1 resolves src -> mid, but p2 resolves nobody (mid not in resolveTo) -> src dropped
            var mid = new TwinEntity().setId(UUID.randomUUID());
            var src = new TwinEntity().setId(UUID.randomUUID());
            var p1 = UUID.randomUUID();
            var p2 = UUID.randomUUID();
            stubSubPointer(p1, 3103);
            stubSubPointer(p2, 3102);
            stubResolve(Map.of(src, mid)); // mid has no further resolution

            Map<UUID, TwinEntity> result = pointer.load(props(p1 + "," + p2), java.util.List.of(src));

            assertTrue(result.isEmpty());
        }
    }
}
