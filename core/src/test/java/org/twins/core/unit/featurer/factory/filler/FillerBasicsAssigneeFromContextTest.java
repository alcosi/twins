package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromContext;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextFieldsAndContextTwinDbFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FillerBasicsAssigneeFromContextTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromContextFieldsAndContextTwinDbFields lookuper;

    private FillerBasicsAssigneeFromContext filler;

    private static final UUID ASSIGNEE_FIELD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerBasicsAssigneeFromContext();
        inject(filler, "fieldLookupers", fieldLookupers);
        when(fieldLookupers.getFromContextFieldsAndContextTwinDbFields()).thenReturn(lookuper);
    }

    private void inject(Object target, String name, Object value) throws Exception {
        Field f = findField(target.getClass(), name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private Field findField(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("field not found: " + name);
    }

    private Properties props() {
        var p = new Properties();
        p.setProperty("assigneeField", ASSIGNEE_FIELD_ID.toString());
        return p;
    }

    private FactoryItem buildFactoryItem() {
        var contextOutput = new TwinCreate();
        contextOutput.setTwinEntity(new TwinEntity());
        var contextItem = new FactoryItem().setOutput(contextOutput);
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(output).setContextFactoryItemList(List.of(contextItem));
    }

    private TwinClassFieldEntity buildField() {
        var field = new TwinClassFieldEntity();
        field.setId(UUID.randomUUID());
        field.setKey("assignee");
        return field;
    }

    @Nested
    class Fill {

        @Test
        void fill_singleUser_setsAssigneeFromContextField() throws ServiceException {
            var factoryItem = buildFactoryItem();
            var user = new UserEntity().setId(UUID.randomUUID());
            var fieldValue = new FieldValueUser(buildField()).add(user);
            when(lookuper.lookupFieldValue(factoryItem, ASSIGNEE_FIELD_ID)).thenReturn(fieldValue);

            filler.fill(props(), List.of(factoryItem), null, false);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertSame(user, outputTwin.getAssignerUser());
            assertEquals(user.getId(), outputTwin.getAssignerUserId());
        }

        @Test
        void fill_emptyUserField_throwsRequired() throws ServiceException {
            var factoryItem = buildFactoryItem();
            var fieldValue = new FieldValueUser(buildField()); // undefined -> empty
            when(lookuper.lookupFieldValue(factoryItem, ASSIGNEE_FIELD_ID)).thenReturn(fieldValue);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_multipleUsers_throwsMultipleNotAllowed() throws ServiceException {
            var factoryItem = buildFactoryItem();
            var fieldValue = new FieldValueUser(buildField())
                    .add(new UserEntity().setId(UUID.randomUUID()))
                    .add(new UserEntity().setId(UUID.randomUUID()));
            when(lookuper.lookupFieldValue(factoryItem, ASSIGNEE_FIELD_ID)).thenReturn(fieldValue);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_nonUserField_throwsIncorrectType() throws ServiceException {
            var factoryItem = buildFactoryItem();
            var fieldValue = new FieldValueText(buildField());
            when(lookuper.lookupFieldValue(factoryItem, ASSIGNEE_FIELD_ID)).thenReturn(fieldValue);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE.getCode(), ex.getErrorCode());
        }
    }
}
