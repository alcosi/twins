package org.twins.core.unit.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.enums.status.StatusType;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.enums.twin.TwinCreateStrategy;
import org.twins.core.featurer.factory.multiplier.MultiplierIsolatedOnContextTwinChildClass;
import org.twins.core.service.twinclass.TwinClassSearchService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiplierIsolatedOnContextTwinChildClassTest extends BaseUnitTest {

    @Mock
    private TwinClassSearchService twinClassSearchService;

    private MultiplierIsolatedOnContextTwinChildClass multiplier;

    @BeforeEach
    void setUp() throws Exception {
        multiplier = new MultiplierIsolatedOnContextTwinChildClass();
        setField(multiplier, "twinClassSearchService", twinClassSearchService);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    private Properties buildProperties(String createStrategy) {
        var props = new Properties();
        if (createStrategy != null) {
            props.put("createStrategy", createStrategy);
        }
        return props;
    }

    private FactoryItem buildInputItem(UUID inputClassId) {
        var twin = new TwinEntity()
                .setId(UUID.randomUUID())
                .setTwinClassId(inputClassId)
                .setTwinClass(new TwinClassEntity().setId(inputClassId))
                .setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
        var twinUpdate = new TwinUpdate().setDbTwinEntity(twin);
        return new FactoryItem().setOutput(twinUpdate);
    }

    @Nested
    class Multiply {

        @Test
        void multiply_singleChildClassFound_newTwinWithThatClassAndHeadPointingToInput() throws ServiceException {
            // contract: "Output class is child of context twin's class" — exactly one child class
            // expected; output twin's headTwin is the input twin.
            var inputClassId = UUID.randomUUID();
            var childClassId = UUID.randomUUID();
            var props = buildProperties(null); // createStrategy defaults to STRICT

            var childClass = new TwinClassEntity().setId(childClassId);
            when(twinClassSearchService.searchTwinClasses(any())).thenReturn(List.of(childClass));

            var input = List.of(buildInputItem(inputClassId));

            var result = multiplier.multiply(props, input, mock(FactoryContext.class));

            assertEquals(1, result.size());
            var out = (TwinCreate) result.get(0).getOutput();
            assertEquals(childClassId, out.getTwinEntity().getTwinClassId());
            assertSame(childClass, out.getTwinEntity().getTwinClass());
            // head twin must point back to the input twin
            assertEquals(input.get(0).getTwin().getId(), out.getTwinEntity().getHeadTwinId());
            assertSame(input.get(0).getTwin(), out.getTwinEntity().getHeadTwin());
            // default createStrategy
            assertEquals(TwinCreateStrategy.STRICT, out.getCreateStrategy());
        }

        @Test
        void multiply_createStrategySketch_propagatedToOutput() throws ServiceException {
            var inputClassId = UUID.randomUUID();
            var props = buildProperties("SKETCH");

            when(twinClassSearchService.searchTwinClasses(any()))
                    .thenReturn(List.of(new TwinClassEntity().setId(UUID.randomUUID())));

            var result = multiplier.multiply(props, List.of(buildInputItem(inputClassId)), mock(FactoryContext.class));

            assertEquals(TwinCreateStrategy.SKETCH, ((TwinCreate) result.get(0).getOutput()).getCreateStrategy());
        }

        @Test
        void multiply_noChildClasses_throwsFactoryMultiplierError() throws ServiceException {
            var inputClassId = UUID.randomUUID();
            when(twinClassSearchService.searchTwinClasses(any())).thenReturn(List.of());

            var ex = assertThrows(ServiceException.class,
                    () -> multiplier.multiply(
                            buildProperties(null),
                            List.of(buildInputItem(inputClassId)),
                            mock(FactoryContext.class)));
            assertEquals(11003, ex.getErrorCode());
        }

        @Test
        void multiply_multipleChildClasses_throwsFactoryMultiplierError() throws ServiceException {
            var inputClassId = UUID.randomUUID();
            when(twinClassSearchService.searchTwinClasses(any())).thenReturn(List.of(
                    new TwinClassEntity().setId(UUID.randomUUID()),
                    new TwinClassEntity().setId(UUID.randomUUID())));

            var ex = assertThrows(ServiceException.class,
                    () -> multiplier.multiply(
                            buildProperties(null),
                            List.of(buildInputItem(inputClassId)),
                            mock(FactoryContext.class)));
            assertEquals(11003, ex.getErrorCode());
        }

        @Test
        void multiply_multiInput_eachInputResolvedIndependently() throws ServiceException {
            // Each input triggers its own searchTwinClasses call (per-input class resolution).
            var classA = UUID.randomUUID();
            var classB = UUID.randomUUID();
            var childA = UUID.randomUUID();
            var childB = UUID.randomUUID();

            when(twinClassSearchService.searchTwinClasses(any()))
                    .thenReturn(List.of(new TwinClassEntity().setId(childA)))
                    .thenReturn(List.of(new TwinClassEntity().setId(childB)));

            var input = List.of(buildInputItem(classA), buildInputItem(classB));

            var result = multiplier.multiply(buildProperties(null), input, mock(FactoryContext.class));

            assertEquals(2, result.size());
            assertEquals(childA, ((TwinCreate) result.get(0).getOutput()).getTwinEntity().getTwinClassId());
            assertEquals(childB, ((TwinCreate) result.get(1).getOutput()).getTwinEntity().getTwinClassId());
            verify(twinClassSearchService, times(2)).searchTwinClasses(any());
        }
    }
}
