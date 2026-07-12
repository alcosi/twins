package org.twins.core.featurer.pointer;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.twin.TwinService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyCollection;
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
    class Load {

        @Test
        void load_mapsSrcTwinToItsHeadTwin() throws ServiceException {
            // Intended: load() loads the head twin for every src twin and maps src id -> head.
            // Calls the protected subclass load(Properties, Collection) directly — the public point(...)
            // entry point now needs a TwinPointerEntity + featurerService, which is out of unit-test scope.
            var headTwin = new TwinEntity();
            var srcTwin = new TwinEntity().setId(UUID.randomUUID());
            srcTwin.setHeadTwinId(UUID.randomUUID());

            // load() now calls the batch overload loadHead(Collection); stub it to populate each twin's head.
            doAnswer(invocation -> {
                Collection<TwinEntity> twins = invocation.getArgument(0);
                for (TwinEntity entity : twins) {
                    entity.setHeadTwin(headTwin);
                }
                return null;
            }).when(twinService).loadHead(anyCollection());

            Map<UUID, TwinEntity> result = pointer.load(new Properties(), List.of(srcTwin));

            assertSame(headTwin, result.get(srcTwin.getId()));
        }
    }
}
