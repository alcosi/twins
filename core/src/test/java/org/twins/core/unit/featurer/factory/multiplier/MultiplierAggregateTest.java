package org.twins.core.unit.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.multiplier.MultiplierAggregate;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiplierAggregateTest extends BaseUnitTest {

    @Mock
    private TwinClassService twinClassService;

    @Mock
    private AuthService authService;

    private MultiplierAggregate multiplier;

    @BeforeEach
    void setUp() throws Exception {
        multiplier = new MultiplierAggregate();
        setField(multiplier, "twinClassService", twinClassService);
        setField(multiplier, "authService", authService);
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

    private Properties buildProperties(UUID outputTwinClassId) {
        var props = new Properties();
        props.put("outputTwinClassId", outputTwinClassId.toString());
        return props;
    }

    private FactoryItem buildInputItem() {
        var twin = new org.twins.core.dao.twin.TwinEntity();
        twin.setId(UUID.randomUUID());
        var twinCreate = new TwinCreate();
        twinCreate.setTwinEntity(twin);
        return new FactoryItem().setOutput(twinCreate);
    }

    @Nested
    class Multiply {

        @Test
        void multiply_multiInput_collapsesIntoSingleOutput() throws ServiceException {
            var outputClassId = UUID.randomUUID();
            var outputClass = new TwinClassEntity();
            outputClass.setId(outputClassId);
            var props = buildProperties(outputClassId);
            var input = List.of(buildInputItem(), buildInputItem(), buildInputItem());

            when(twinClassService.findEntitySafe(outputClassId)).thenReturn(outputClass);
            var apiUser = mock(ApiUser.class);
            when(apiUser.getUser()).thenReturn(new UserEntity());
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = multiplier.multiply(props, input, mock(FactoryContext.class));

            // contract: "Only one output twin, even for multiple input"
            assertEquals(1, result.size());
            var output = result.get(0);
            assertInstanceOf(TwinCreate.class, output.getOutput());
            // the single aggregated item must reference ALL inputs as its context
            assertSame(input, output.getContextFactoryItemList());
            assertEquals(3, output.getContextFactoryItemList().size());
            assertEquals(outputClassId, ((TwinCreate) output.getOutput()).getTwinEntity().getTwinClassId());
        }

        @Test
        void multiply_singleInput_producesSingleOutput() throws ServiceException {
            var outputClassId = UUID.randomUUID();
            var outputClass = new TwinClassEntity();
            outputClass.setId(outputClassId);
            var props = buildProperties(outputClassId);

            when(twinClassService.findEntitySafe(outputClassId)).thenReturn(outputClass);
            var apiUser = mock(ApiUser.class);
            when(apiUser.getUser()).thenReturn(new UserEntity());
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = multiplier.multiply(props, List.of(buildInputItem()), mock(FactoryContext.class));

            assertEquals(1, result.size());
        }

        @Test
        void multiply_emptyInput_stillProducesSingleAggregatedOutput() throws ServiceException {
            var outputClassId = UUID.randomUUID();
            var outputClass = new TwinClassEntity();
            outputClass.setId(outputClassId);
            var props = buildProperties(outputClassId);

            when(twinClassService.findEntitySafe(outputClassId)).thenReturn(outputClass);
            var apiUser = mock(ApiUser.class);
            when(apiUser.getUser()).thenReturn(new UserEntity());
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = multiplier.multiply(props, List.of(), mock(FactoryContext.class));

            // Aggregate creates exactly one twin unconditionally — empty input yields one aggregated item
            // whose context list is the (empty) input list, not zero items.
            assertEquals(1, result.size());
            assertTrue(result.get(0).getContextFactoryItemList().isEmpty());
        }
    }
}
