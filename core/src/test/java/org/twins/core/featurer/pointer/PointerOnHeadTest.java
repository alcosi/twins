package org.twins.core.featurer.pointer;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doAnswer;

class PointerOnHeadTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private PointerOnHead pointer;

    @BeforeEach
    void setUp() {
        pointer = new PointerOnHead(twinService);
    }

    @Nested
    class Point {

        @Test
        void point_returnsHeadTwin() throws ServiceException {
            var headTwin = new TwinEntity();
            var srcTwin = new TwinEntity();
            srcTwin.setHeadTwinId(UUID.randomUUID());

            doAnswer(invocation -> {
                TwinEntity entity = invocation.getArgument(0);
                entity.setHeadTwin(headTwin);
                return null;
            }).when(twinService).loadHeadForTwin(srcTwin);

            var result = pointer.point(new Properties(), srcTwin);

            assertSame(headTwin, result);
        }
    }
}
