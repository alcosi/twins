package org.twins.core.featurer.pointer;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.pointer.PointerOnGivenTwin;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

class PointerOnGivenTwinTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private PointerOnGivenTwin pointer;

    @BeforeEach
    void setUp() {
        pointer = new PointerOnGivenTwin(twinService);
    }

    private Properties props(String twinId) {
        var props = new Properties();
        props.put("twinId", twinId);
        return props;
    }

    @Nested
    class Point {

        @Test
        void point_returnsGivenTwin() throws ServiceException {
            var targetTwinId = UUID.randomUUID();
            var targetTwin = new TwinEntity();

            when(twinService.findEntitySafe(targetTwinId)).thenReturn(targetTwin);

            var result = pointer.point(
                    props(targetTwinId.toString()),
                    new TwinEntity()
            );

            assertSame(targetTwin, result);
        }
    }
}
