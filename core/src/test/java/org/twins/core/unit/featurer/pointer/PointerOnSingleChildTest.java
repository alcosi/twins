package org.twins.core.featurer.pointer;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.pointer.PointerOnSingleChild;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

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
    class Point {

        @Test
        void point_noChildren_returnsNull() throws ServiceException {
            var srcTwin = new TwinEntity();
            when(twinSearchService.findTwins(org.mockito.ArgumentMatchers.any())).thenReturn(Collections.emptyList());

            var result = pointer.point(props(), srcTwin);

            assertNull(result);
        }

        @Test
        void point_singleChild_returnsChild() throws ServiceException {
            var childTwin = new TwinEntity();
            var srcTwin = new TwinEntity();
            when(twinSearchService.findTwins(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(childTwin));

            var result = pointer.point(props(), srcTwin);

            assertSame(childTwin, result);
        }

        @Test
        void point_multipleChildren_throwsPointerNonSingle() throws ServiceException {
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
