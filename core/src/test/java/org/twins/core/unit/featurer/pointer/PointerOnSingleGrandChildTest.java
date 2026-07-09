package org.twins.core.featurer.pointer;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.pointer.PointerOnSingleGrandChild;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

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
    class Point {

        @Test
        void point_noGrandchildren_returnsNull() throws ServiceException {
            var srcTwin = new TwinEntity();
            when(twinSearchService.findTwins(org.mockito.ArgumentMatchers.any())).thenReturn(Collections.emptyList());

            var result = pointer.point(props(), srcTwin);

            assertNull(result);
        }

        @Test
        void point_singleGrandchild_returnsGrandchild() throws ServiceException {
            var grandchild = new TwinEntity();
            var srcTwin = new TwinEntity();
            when(twinSearchService.findTwins(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(grandchild));

            var result = pointer.point(props(), srcTwin);

            assertSame(grandchild, result);
        }

        @Test
        void point_multipleGrandchildren_throwsPointerNonSingle() throws ServiceException {
            var srcTwin = new TwinEntity();
            when(twinSearchService.findTwins(org.mockito.ArgumentMatchers.any()))
                    .thenReturn(List.of(new TwinEntity(), new TwinEntity()));

            var ex = assertThrows(
                    ServiceException.class,
                    () -> pointer.point(props(), srcTwin)
            );
            assertEquals(ErrorCodeTwins.POINTER_NON_SINGLE.getCode(), ex.getErrorCode());
        }
    }
}
