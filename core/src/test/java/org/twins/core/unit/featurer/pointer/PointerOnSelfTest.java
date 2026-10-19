package org.twins.core.featurer.pointer;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;


class PointerOnSelfTest extends BaseUnitTest {

    private final PointerOnSelf pointer = new PointerOnSelf();

    @Nested
    class Load {

        @Test
        void load_mapsEverySrcTwinToItself() throws ServiceException {
            // Intended: a self-pointer resolves every src twin to itself.
            // Calls the protected subclass load(Properties, Collection) directly — the public point(...)
            // entry point now needs a TwinPointerEntity + featurerService, which is out of unit-test scope.
            var twin = new TwinEntity().setId(UUID.randomUUID());

            Map<UUID, TwinEntity> result = pointer.load(new Properties(), List.of(twin));

            assertSame(twin, result.get(twin.getId()));
        }
    }
}
