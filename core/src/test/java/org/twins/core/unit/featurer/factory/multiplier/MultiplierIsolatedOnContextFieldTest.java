package org.twins.core.unit.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.multiplier.MultiplierIsolatedOnContextField;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiplierIsolatedOnContextFieldTest extends BaseUnitTest {

    @Mock
    private TwinClassService twinClassService;

    @Mock
    private AuthService authService;

    private MultiplierIsolatedOnContextField multiplier;

    @BeforeEach
    void setUp() throws Exception {
        multiplier = new MultiplierIsolatedOnContextField();
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

    private Properties buildProperties(UUID contextFieldId, UUID elseClassId) {
        var props = new Properties();
        props.put("outputTwinClassIdFromContextField", contextFieldId.toString());
        props.put("elseOutputTwinClassId", elseClassId.toString());
        return props;
    }

    private FactoryItem buildInputItem() {
        var twin = new TwinEntity().setId(UUID.randomUUID());
        var twinCreate = new TwinCreate();
        twinCreate.setTwinEntity(twin);
        return new FactoryItem().setOutput(twinCreate);
    }

    private ApiUser stubApiUser() throws ServiceException {
        var apiUser = mock(ApiUser.class);
        when(apiUser.getUser()).thenReturn(new UserEntity().setId(UUID.randomUUID()));
        return apiUser;
    }

    /**
     * Minimal concrete FieldValue stub. The multiplier only reads
     * {@code getTwinClassField().getTwinClassId()} so the abstract value side is unused.
     */
    private FieldValue buildFieldValue(UUID twinClassId) {
        var field = new TwinClassFieldEntity();
        field.setTwinClassId(twinClassId);
        return new FieldValue(field) {
            @Override public FieldValue newInstance(TwinClassFieldEntity newField) { return this; }
            @Override public boolean hasValue(String value) { return false; }
            @Override public void copyValueTo(FieldValue dst) { }
            @Override public FieldValue undefine() { return this; }
            @Override public boolean isUndefined() { return true; }
            @Override public FieldValue clear() { return this; }
            @Override public boolean isCleared() { return false; }
        };
    }

    @Nested
    class Multiply {

        @Test
        void multiply_contextFieldPresent_usesItsTwinClass() throws ServiceException {
            var contextFieldId = UUID.randomUUID();
            var classFromField = UUID.randomUUID();
            var elseClass = UUID.randomUUID();
            var props = buildProperties(contextFieldId, elseClass);

            var ctx = mock(FactoryContext.class);
            Map<UUID, FieldValue> fields = new HashMap<>();
            fields.put(contextFieldId, buildFieldValue(classFromField));
            when(ctx.getFields()).thenReturn(fields);

            var classEntity = new TwinClassEntity().setId(classFromField);
            when(twinClassService.findEntitySafe(classFromField)).thenReturn(classEntity);
            var apiUser = stubApiUser();
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = multiplier.multiply(props, List.of(buildInputItem()), ctx);

            assertEquals(1, result.size());
            var out = (TwinCreate) result.get(0).getOutput();
            assertEquals(classFromField, out.getTwinEntity().getTwinClassId());
            // else branch must NOT have been consulted
            verify(twinClassService, never()).findEntitySafe(elseClass);
        }

        @Test
        void multiply_contextFieldAbsent_fallsBackToElseClass() throws ServiceException {
            var contextFieldId = UUID.randomUUID();
            var elseClass = UUID.randomUUID();
            var props = buildProperties(contextFieldId, elseClass);

            var ctx = mock(FactoryContext.class);
            when(ctx.getFields()).thenReturn(new HashMap<>()); // field not present

            var classEntity = new TwinClassEntity().setId(elseClass);
            when(twinClassService.findEntitySafe(elseClass)).thenReturn(classEntity);
            var apiUser = stubApiUser();
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = multiplier.multiply(props, List.of(buildInputItem()), ctx);

            assertEquals(1, result.size());
            var out = (TwinCreate) result.get(0).getOutput();
            assertEquals(elseClass, out.getTwinEntity().getTwinClassId());
            verify(twinClassService).findEntitySafe(elseClass);
        }

        @Test
        void multiply_multiInput_producesOnePerInputAllSharingResolvedClass() throws ServiceException {
            var contextFieldId = UUID.randomUUID();
            var classFromField = UUID.randomUUID();
            var elseClass = UUID.randomUUID();
            var props = buildProperties(contextFieldId, elseClass);

            var ctx = mock(FactoryContext.class);
            Map<UUID, FieldValue> fields = new HashMap<>();
            fields.put(contextFieldId, buildFieldValue(classFromField));
            when(ctx.getFields()).thenReturn(fields);

            when(twinClassService.findEntitySafe(classFromField))
                    .thenReturn(new TwinClassEntity().setId(classFromField));
            var apiUser = stubApiUser();
            when(authService.getApiUser()).thenReturn(apiUser);

            var input = List.of(buildInputItem(), buildInputItem());

            var result = multiplier.multiply(props, input, ctx);

            assertEquals(2, result.size());
            for (int i = 0; i < result.size(); i++) {
                var out = (TwinCreate) result.get(i).getOutput();
                assertEquals(classFromField, out.getTwinEntity().getTwinClassId());
                assertSame(input.get(i), result.get(i).getContextFactoryItemList().get(0));
            }
            // class resolved once outside the per-input loop
            verify(twinClassService, times(1)).findEntitySafe(classFromField);
        }

        @Test
        void multiply_emptyInput_producesNoOutput() throws ServiceException {
            var contextFieldId = UUID.randomUUID();
            var elseClass = UUID.randomUUID();
            var props = buildProperties(contextFieldId, elseClass);

            var ctx = mock(FactoryContext.class);
            when(ctx.getFields()).thenReturn(new HashMap<>());
            when(twinClassService.findEntitySafe(elseClass)).thenReturn(new TwinClassEntity().setId(elseClass));

            var result = multiplier.multiply(props, List.of(), ctx);

            assertTrue(result.isEmpty());
        }
    }
}
