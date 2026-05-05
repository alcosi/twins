package org.twins.core.featurer.pointer;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertSame;

class PointerOnSelfTest extends BaseUnitTest {

    private final PointerOnSelf pointer = new PointerOnSelf();

    @Nested
    class Point {

        @Test
        void point_returnsSourceTwin() throws ServiceException {
            var twin = new TwinEntity();

            var result = pointer.point(new Properties(), twin);

            assertSame(twin, result);
        }
    }
}
