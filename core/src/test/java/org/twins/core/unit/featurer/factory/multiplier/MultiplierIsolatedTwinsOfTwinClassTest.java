package org.twins.core.unit.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.enums.status.StatusType;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.factory.multiplier.MultiplierIsolatedTwinsOfTwinClass;
import org.twins.core.service.twin.TwinSearchService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiplierIsolatedTwinsOfTwinClassTest extends BaseUnitTest {

    @Mock
    private TwinSearchService twinSearchService;

    private MultiplierIsolatedTwinsOfTwinClass multiplier;

    @BeforeEach
    void setUp() throws Exception {
        multiplier = new MultiplierIsolatedTwinsOfTwinClass(twinSearchService);
    }

    private Properties buildProperties(UUID twinClassId) {
        var props = new Properties();
        props.put("twinClassId", twinClassId.toString());
        return props;
    }

    private FactoryItem buildInputItem() {
        var twin = new TwinEntity()
                .setId(UUID.randomUUID())
                .setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
        var twinUpdate = new TwinUpdate().setDbTwinEntity(twin);
        return new FactoryItem().setOutput(twinUpdate);
    }

    // twins are cloned in the loop; clone triggers isSketch() which reads twinStatus.getType(),
    // so every twin under test must carry a non-null status.
    private TwinEntity buildTwin() {
        var twin = new TwinEntity();
        twin.setId(UUID.randomUUID());
        twin.setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
        return twin;
    }

    @Nested
    class Multiply {

        @Test
        void multiply_findsTwinsOfClass_producesOneOutputPerFoundTwin() throws ServiceException {
            // contract: "Twins will be loaded by twin class from params" — output count == found twins.
            var classId = UUID.randomUUID();
            var props = buildProperties(classId);
            var ctx = mock(FactoryContext.class);
            when(ctx.getRunLimitedByOwnerBusinessAccount()).thenReturn(null);

            var twin1 = buildTwin();
            var twin2 = buildTwin();
            var twin3 = buildTwin();
            when(twinSearchService.findTwins(any(BasicSearch.class)))
                    .thenReturn(List.of(twin1, twin2, twin3));

            var input = List.of(buildInputItem());

            var result = multiplier.multiply(props, input, ctx);

            assertEquals(3, result.size());
            assertSame(twin1, ((TwinUpdate) result.get(0).getOutput()).getDbTwinEntity());
            assertSame(twin2, ((TwinUpdate) result.get(1).getOutput()).getDbTwinEntity());
            assertSame(twin3, ((TwinUpdate) result.get(2).getOutput()).getDbTwinEntity());
        }

        @Test
        void multiply_contextListReferencesTheWholeInputList() throws ServiceException {
            // Unlike isolated-by-input multipliers, here every output's context list IS the
            // entire input list (the twins are global to the class, not bound to a specific input).
            var classId = UUID.randomUUID();
            var ctx = mock(FactoryContext.class);
            when(ctx.getRunLimitedByOwnerBusinessAccount()).thenReturn(null);
            when(twinSearchService.findTwins(any(BasicSearch.class)))
                    .thenReturn(List.of(buildTwin()));

            var input = List.of(buildInputItem(), buildInputItem(), buildInputItem());

            var result = multiplier.multiply(buildProperties(classId), input, ctx);

            assertEquals(1, result.size());
            assertSame(input, result.get(0).getContextFactoryItemList());
            assertEquals(3, result.get(0).getContextFactoryItemList().size());
        }

        @Test
        void multiply_noTwinsFound_returnsEmpty() throws ServiceException {
            var classId = UUID.randomUUID();
            var ctx = mock(FactoryContext.class);
            when(ctx.getRunLimitedByOwnerBusinessAccount()).thenReturn(null);
            when(twinSearchService.findTwins(any(BasicSearch.class)))
                    .thenReturn(List.of());

            var result = multiplier.multiply(buildProperties(classId), List.of(buildInputItem()), ctx);

            assertTrue(result.isEmpty());
        }

        @Test
        void multiply_twinClassIdPropagatedToSearch() throws ServiceException {
            var classId = UUID.randomUUID();
            var ctx = mock(FactoryContext.class);
            when(ctx.getRunLimitedByOwnerBusinessAccount()).thenReturn(null);
            when(twinSearchService.findTwins(any(BasicSearch.class)))
                    .thenReturn(List.of());

            multiplier.multiply(buildProperties(classId), List.of(buildInputItem()), ctx);

            var captor = org.mockito.ArgumentCaptor.forClass(BasicSearch.class);
            verify(twinSearchService).findTwins(captor.capture());
            assertNotNull(captor.getValue().getTwinClassExtendsHierarchyContainsIdList());
            assertTrue(captor.getValue().getTwinClassExtendsHierarchyContainsIdList().contains(classId));
        }
    }
}
