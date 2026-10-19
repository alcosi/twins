package org.twins.core.featurer.pointer;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.twin.TwinService;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

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
    class Load {

        @Test
        void load_resolvesGivenTwinForEverySrcTwin() throws ServiceException {
            // Intended: load() looks up the configured target twin once and maps every src twin id to it.
            // Calls the protected subclass load(Properties, Collection) directly — the public point(...)
            // entry point now needs a TwinPointerEntity + featurerService, which is out of unit-test scope.
            var targetTwinId = UUID.randomUUID();
            var targetTwin = new TwinEntity();
            var srcTwin = new TwinEntity().setId(UUID.randomUUID());

            when(twinService.findEntitySafe(targetTwinId)).thenReturn(targetTwin);

            Map<UUID, TwinEntity> result = pointer.load(props(targetTwinId.toString()), List.of(srcTwin));

            assertSame(targetTwin, result.get(srcTwin.getId()));
        }
    }
}
