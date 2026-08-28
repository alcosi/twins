package org.twins.core.unit.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.multiplier.MultiplierIsolated;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiplierIsolatedTest extends BaseUnitTest {

    @Mock
    private TwinClassService twinClassService;

    @Mock
    private AuthService authService;

    private MultiplierIsolated multiplier;

    @BeforeEach
    void setUp() throws Exception {
        multiplier = new MultiplierIsolated();
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

    private TwinClassEntity buildTwinClass(UUID id) {
        var twinClass = new TwinClassEntity();
        twinClass.setId(id);
        return twinClass;
    }

    private FactoryItem buildInputItem() {
        var twin = new TwinEntity();
        twin.setId(UUID.randomUUID());
        var twinCreate = new TwinCreate();
        twinCreate.setTwinEntity(twin);
        return new FactoryItem().setOutput(twinCreate);
    }

    @Nested
    class Multiply {

        @Test
        void multiply_multiInput_producesOneOutputPerInput() throws ServiceException {
            var outputClassId = UUID.randomUUID();
            var outputClass = buildTwinClass(outputClassId);
            var props = buildProperties(outputClassId);
            var input = List.of(buildInputItem(), buildInputItem(), buildInputItem());

            when(twinClassService.findEntitySafe(outputClassId)).thenReturn(outputClass);
            var apiUser = mock(ApiUser.class);
            var user = new UserEntity();
            user.setId(UUID.randomUUID());
            when(apiUser.getUser()).thenReturn(user);
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = multiplier.multiply(props, input, mock(FactoryContext.class));

            assertEquals(3, result.size());

            // the output twin class is resolved from the param once, then reused for all N outputs
            verify(twinClassService, times(1)).findEntitySafe(outputClassId);
            for (var item : result) {
                assertInstanceOf(TwinCreate.class, item.getOutput());
            }
            // each output item is wired to exactly its single source input (isolated scope)
            for (int i = 0; i < result.size(); i++) {
                assertEquals(1, result.get(i).getContextFactoryItemList().size());
                assertSame(input.get(i), result.get(i).getContextFactoryItemList().get(0));
                assertEquals(outputClassId, ((TwinCreate) result.get(i).getOutput()).getTwinEntity().getTwinClassId());
            }
        }

        @Test
        void multiply_singleInput_producesSingleOutput() throws ServiceException {
            var outputClassId = UUID.randomUUID();
            var outputClass = buildTwinClass(outputClassId);
            var props = buildProperties(outputClassId);
            var input = List.of(buildInputItem());

            when(twinClassService.findEntitySafe(outputClassId)).thenReturn(outputClass);
            var apiUser = mock(ApiUser.class);
            when(apiUser.getUser()).thenReturn(new UserEntity());
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = multiplier.multiply(props, input, mock(FactoryContext.class));

            assertEquals(1, result.size());
        }

        @Test
        void multiply_emptyInput_producesNoOutput() throws ServiceException {
            var outputClassId = UUID.randomUUID();
            var outputClass = buildTwinClass(outputClassId);
            var props = buildProperties(outputClassId);

            // empty input -> the per-input loop body (which reads the api user) never runs,
            // so only the output-class lookup (performed once before the loop) is needed.
            when(twinClassService.findEntitySafe(outputClassId)).thenReturn(outputClass);

            var result = multiplier.multiply(props, List.of(), mock(FactoryContext.class));

            assertTrue(result.isEmpty());
        }
    }
}
