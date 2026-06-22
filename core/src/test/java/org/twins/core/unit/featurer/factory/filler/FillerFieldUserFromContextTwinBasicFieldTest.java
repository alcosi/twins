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
import org.twins.core.domain.TwinBasicFields;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerFieldUserFromContextTwinBasicField;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FillerFieldUserFromContextTwinBasicFieldTest extends BaseUnitTest {

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FillerFieldUserFromContextTwinBasicField filler;

    private static final UUID FIELD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerFieldUserFromContextTwinBasicField();
        inject(filler, "twinClassFieldService", twinClassFieldService);
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

    private Properties props(TwinBasicFields.Basics basic) {
        var p = new Properties();
        p.setProperty("twinClassFieldId", FIELD_ID.toString());
        p.setProperty("field", basic.name());
        return p;
    }

    private FactoryItem buildFactoryItem(TwinEntity contextTwin) {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        var contextOutput = new TwinCreate();
        contextOutput.setTwinEntity(contextTwin);
        var contextItem = new FactoryItem().setOutput(contextOutput);
        return new FactoryItem().setOutput(output).setContextFactoryItemList(List.of(contextItem));
    }

    private TwinClassFieldEntity fieldEntity() {
        return new TwinClassFieldEntity().setId(FIELD_ID);
    }

    @Nested
    class Fill {

        @Test
        void fill_assigneeBasicField_writesContextTwinAssignee() throws ServiceException {
            // NAME promises: write the CONTEXT TWIN's basic-field user (here: assignee) into the user field.
            var assignee = new UserEntity().setId(UUID.randomUUID());
            var contextTwin = new TwinEntity()
                    .setAssignerUser(assignee)
                    .setAssignerUserId(assignee.getId());
            var factoryItem = buildFactoryItem(contextTwin);
            when(twinClassFieldService.findEntitySafe(FIELD_ID)).thenReturn(fieldEntity());

            filler.fill(props(TwinBasicFields.Basics.assigneeUserId), factoryItem, null);

            FieldValueUser stored = (FieldValueUser) factoryItem.getOutput().getField(FIELD_ID);
            assertEquals(1, stored.size());
            assertSame(assignee, stored.getItems().getFirst());
        }

        @Test
        void fill_createdByBasicField_writesContextTwinCreator() throws ServiceException {
            var creator = new UserEntity().setId(UUID.randomUUID());
            var contextTwin = new TwinEntity()
                    .setCreatedByUser(creator)
                    .setCreatedByUserId(creator.getId());
            var factoryItem = buildFactoryItem(contextTwin);
            when(twinClassFieldService.findEntitySafe(FIELD_ID)).thenReturn(fieldEntity());

            filler.fill(props(TwinBasicFields.Basics.createdByUserId), factoryItem, null);

            FieldValueUser stored = (FieldValueUser) factoryItem.getOutput().getField(FIELD_ID);
            assertEquals(1, stored.size());
            assertSame(creator, stored.getItems().getFirst());
        }

        @Test
        void fill_assigneeNull_throwsStepError() throws ServiceException {
            var contextTwin = new TwinEntity(); // assignerUserId == null
            var factoryItem = buildFactoryItem(contextTwin);
            when(twinClassFieldService.findEntitySafe(FIELD_ID)).thenReturn(fieldEntity());

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(TwinBasicFields.Basics.assigneeUserId), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_createdByNull_throwsStepError() throws ServiceException {
            var contextTwin = new TwinEntity(); // createdByUserId == null
            var factoryItem = buildFactoryItem(contextTwin);
            when(twinClassFieldService.findEntitySafe(FIELD_ID)).thenReturn(fieldEntity());

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(TwinBasicFields.Basics.createdByUserId), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_nameBasicField_throwsUnknownBasicField() throws ServiceException {
            // NAME + switch default promise: only assignee/createdBy are supported; other Basics -> TWIN_BASIC_FIELD_UNKNOWN.
            var contextTwin = new TwinEntity();
            var factoryItem = buildFactoryItem(contextTwin);
            when(twinClassFieldService.findEntitySafe(FIELD_ID)).thenReturn(fieldEntity());

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(TwinBasicFields.Basics.name), factoryItem, null));
            assertEquals(ErrorCodeTwins.TWIN_BASIC_FIELD_UNKNOWN.getCode(), ex.getErrorCode());
        }
    }
}
