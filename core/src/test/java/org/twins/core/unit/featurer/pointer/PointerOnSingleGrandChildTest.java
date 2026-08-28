package org.twins.core.featurer.pointer;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.twin.TwinSearchService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PointerOnSingleGrandChildTest extends BaseUnitTest {

    @Mock
    private TwinSearchService twinSearchService;

    private PointerOnSingleGrandChild pointer;

    private UUID twinClassId;

    @BeforeEach
    void setUp() {
        pointer = new PointerOnSingleGrandChild(twinSearchService);
        twinClassId = UUID.randomUUID();
    }

    private Properties props() {
        var props = new Properties();
        props.put("grandChildTwinClassId", twinClassId.toString());
        return props;
    }

    @Nested
    class Load {

        @Test
        void load_noGrandchildren_returnsEmptyMapping() throws ServiceException {
            var srcTwin = new TwinEntity().setId(UUID.randomUUID());
            when(twinSearchService.findTwins(org.mockito.ArgumentMatchers.any())).thenReturn(Collections.emptyList());

            Map<UUID, TwinEntity> result = pointer.load(props(), List.of(srcTwin));

            assertNull(result.get(srcTwin.getId()));
        }

        @Test
        void load_singleGrandchild_returnsGrandchild() throws ServiceException {
            var srcTwin = new TwinEntity().setId(UUID.randomUUID());
            var grandchild = new TwinEntity();
            grandchild.setHeadTwinsIdSet(Set.of(srcTwin.getId())); // grandchild is attributed to src via its ancestor set
            when(twinSearchService.findTwins(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(grandchild));

            Map<UUID, TwinEntity> result = pointer.load(props(), List.of(srcTwin));

            assertSame(grandchild, result.get(srcTwin.getId()));
        }

        @Test
        void load_multipleGrandchildren_throwsPointerNonSingle() throws ServiceException {
            var srcTwin = new TwinEntity().setId(UUID.randomUUID());
            var g1 = new TwinEntity();
            g1.setHeadTwinsIdSet(Set.of(srcTwin.getId()));
            var g2 = new TwinEntity();
            g2.setHeadTwinsIdSet(Set.of(srcTwin.getId()));
            when(twinSearchService.findTwins(org.mockito.ArgumentMatchers.any()))
                    .thenReturn(List.of(g1, g2));

            var ex = assertThrows(
                    ServiceException.class,
                    () -> pointer.load(props(), List.of(srcTwin))
            );
            assertEquals(ErrorCodeTwins.POINTER_NON_SINGLE.getCode(), ex.getErrorCode());
        }
    }
}
