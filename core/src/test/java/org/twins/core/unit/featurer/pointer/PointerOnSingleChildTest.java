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

class PointerOnSingleChildTest extends BaseUnitTest {

    @Mock
    private TwinSearchService twinSearchService;

    private PointerOnSingleChild pointer;

    private UUID twinClassId;

    @BeforeEach
    void setUp() {
        pointer = new PointerOnSingleChild(twinSearchService);
        twinClassId = UUID.randomUUID();
    }

    private Properties props() {
        var props = new Properties();
        props.put("childTwinClassId", twinClassId.toString());
        return props;
    }

    @Nested
    class Load {

        @Test
        void load_noChildren_returnsEmptyMapping() throws ServiceException {
            var srcTwin = new TwinEntity().setId(UUID.randomUUID());
            when(twinSearchService.findTwins(org.mockito.ArgumentMatchers.any())).thenReturn(Collections.emptyList());

            Map<UUID, TwinEntity> result = pointer.load(props(), List.of(srcTwin));

            assertNull(result.get(srcTwin.getId()));
        }

        @Test
        void load_singleChild_returnsChild() throws ServiceException {
            var srcTwin = new TwinEntity().setId(UUID.randomUUID());
            var childTwin = new TwinEntity();
            childTwin.setHeadTwinId(srcTwin.getId()); // batch grouping keys children by headTwinId
            when(twinSearchService.findTwins(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(childTwin));

            Map<UUID, TwinEntity> result = pointer.load(props(), List.of(srcTwin));

            assertSame(childTwin, result.get(srcTwin.getId()));
        }

        @Test
        void load_multipleChildren_throwsPointerNonSingle() throws ServiceException {
            var srcTwin = new TwinEntity().setId(UUID.randomUUID());
            var child1 = new TwinEntity();
            child1.setHeadTwinId(srcTwin.getId());
            var child2 = new TwinEntity();
            child2.setHeadTwinId(srcTwin.getId());
            when(twinSearchService.findTwins(org.mockito.ArgumentMatchers.any()))
                    .thenReturn(List.of(child1, child2));

            var ex = assertThrows(
                    ServiceException.class,
                    () -> pointer.load(props(), List.of(srcTwin))
            );
            assertEquals(ErrorCodeTwins.POINTER_NON_SINGLE.getCode(), ex.getErrorCode());
        }
    }
}
