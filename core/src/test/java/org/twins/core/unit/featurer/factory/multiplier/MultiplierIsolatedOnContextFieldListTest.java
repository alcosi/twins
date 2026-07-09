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
import org.twins.core.featurer.factory.multiplier.MultiplierIsolatedOnContextFieldList;
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

class MultiplierIsolatedOnContextFieldListTest extends BaseUnitTest {

    @Mock
    private TwinClassService twinClassService;

    @Mock
    private AuthService authService;

    private MultiplierIsolatedOnContextFieldList multiplier;

    @BeforeEach
    void setUp() throws Exception {
        multiplier = new MultiplierIsolatedOnContextFieldList();
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

    private Properties buildProperties(List<UUID> fieldIdList) {
        // FeaturerParamUUIDSet splits the string on commas; order preserved via LinkedHashSet.
        var joined = new StringBuilder();
        for (int i = 0; i < fieldIdList.size(); i++) {
            if (i > 0) joined.append(",");
            joined.append(fieldIdList.get(i).toString());
        }
        var props = new Properties();
        props.put("contextFieldIdList", joined.toString());
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
        void multiply_multiplePresentFields_lastOneWins() throws ServiceException {
            // contract: loop iterates the configured list in order; the LAST present field's class wins.
            var f1 = UUID.randomUUID();
            var f2 = UUID.randomUUID();
            var f3 = UUID.randomUUID();
            var class1 = UUID.randomUUID();
            var class3 = UUID.randomUUID();
            var props = buildProperties(List.of(f1, f2, f3));

            var ctx = mock(FactoryContext.class);
            Map<UUID, FieldValue> fields = new HashMap<>();
            fields.put(f1, buildFieldValue(class1));
            fields.put(f3, buildFieldValue(class3)); // f2 absent, f3 last -> wins
            when(ctx.getFields()).thenReturn(fields);

            when(twinClassService.findEntitySafe(class3)).thenReturn(new TwinClassEntity().setId(class3));
            var apiUser = stubApiUser();
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = multiplier.multiply(props, List.of(buildInputItem()), ctx);

            assertEquals(1, result.size());
            var out = (TwinCreate) result.get(0).getOutput();
            assertEquals(class3, out.getTwinEntity().getTwinClassId());
            verify(twinClassService, never()).findEntitySafe(class1);
        }

        @Test
        void expand_perListElement_classResolvedFromSinglePresentField() throws ServiceException {
            // Per the brief: "OnContextFieldList expands per list element" — the list is iterated
            // until a present field is found; here only one is present, so its class is used.
            var f1 = UUID.randomUUID();
            var f2 = UUID.randomUUID();
            var class2 = UUID.randomUUID();
            var props = buildProperties(List.of(f1, f2));

            var ctx = mock(FactoryContext.class);
            Map<UUID, FieldValue> fields = new HashMap<>();
            fields.put(f2, buildFieldValue(class2));
            when(ctx.getFields()).thenReturn(fields);

            when(twinClassService.findEntitySafe(class2)).thenReturn(new TwinClassEntity().setId(class2));
            var apiUser = stubApiUser();
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = multiplier.multiply(props, List.of(buildInputItem()), ctx);

            assertEquals(1, result.size());
            var out = (TwinCreate) result.get(0).getOutput();
            assertEquals(class2, out.getTwinEntity().getTwinClassId());
        }

        @Test
        void multiply_noFieldPresent_throwsFactoryMultiplierError() {
            var f1 = UUID.randomUUID();
            var f2 = UUID.randomUUID();
            var props = buildProperties(List.of(f1, f2));

            var ctx = mock(FactoryContext.class);
            when(ctx.getFields()).thenReturn(new HashMap<>());

            var ex = assertThrows(ServiceException.class,
                    () -> multiplier.multiply(props, List.of(buildInputItem()), ctx));
            assertEquals(11003, ex.getErrorCode());
        }

        @Test
        void multiply_emptyFieldList_throwsFactoryMultiplierError() {
            // An empty configured list resolves no class — same error path as no field present.
            // The list is checked before any field lookup, so ctx.getFields() is never consulted.
            var props = buildProperties(List.of());
            var ctx = mock(FactoryContext.class);

            var ex = assertThrows(ServiceException.class,
                    () -> multiplier.multiply(props, List.of(buildInputItem()), ctx));
            assertEquals(11003, ex.getErrorCode());
        }

        @Test
        void multiply_multiInput_eachOutputWiredToItsOwnInput() throws ServiceException {
            var f1 = UUID.randomUUID();
            var class1 = UUID.randomUUID();
            var props = buildProperties(List.of(f1));

            var ctx = mock(FactoryContext.class);
            Map<UUID, FieldValue> fields = new HashMap<>();
            fields.put(f1, buildFieldValue(class1));
            when(ctx.getFields()).thenReturn(fields);

            when(twinClassService.findEntitySafe(class1)).thenReturn(new TwinClassEntity().setId(class1));
            var apiUser = stubApiUser();
            when(authService.getApiUser()).thenReturn(apiUser);

            var input = List.of(buildInputItem(), buildInputItem());

            var result = multiplier.multiply(props, input, ctx);

            assertEquals(2, result.size());
            for (int i = 0; i < result.size(); i++) {
                assertSame(input.get(i), result.get(i).getContextFactoryItemList().get(0));
            }
        }
    }
}
