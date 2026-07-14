package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerComment;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextTwinDbFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FillerCommentTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromContextTwinDbFields lookuper;

    private FillerComment filler;

    private static final UUID FIELD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerComment();
        inject(filler, "fieldLookupers", fieldLookupers);
        when(fieldLookupers.getFromContextTwinDbFields()).thenReturn(lookuper);
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
        p.setProperty("fieldId", FIELD_ID.toString());
        return p;
    }

    private FactoryItem buildFactoryItem() {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(output);
    }

    private TwinClassFieldEntity field() {
        return new TwinClassFieldEntity().setId(FIELD_ID).setTwinClassId(UUID.randomUUID());
    }

    @Nested
    class Fill {

        @Test
        void fill_textField_addsValueAsComment() throws ServiceException {
            // NAME promises: take the (text) field value from the context twin DB and add it as a comment to the output.
            var factoryItem = buildFactoryItem();
            FieldValue value = new FieldValueText(field()).setValue("hello world");
            when(lookuper.lookupFieldValue(factoryItem, FIELD_ID)).thenReturn(value);

            filler.fill(props(), List.of(factoryItem), null, false);

            var create = (TwinCreate) factoryItem.getOutput();
            assertNotNull(create.getCommentsAdd());
            assertTrue(create.getCommentsAdd().contains("hello world"));
        }

        @Test
        void fill_nonTextField_throwsStepError() throws ServiceException {
            var factoryItem = buildFactoryItem();
            FieldValue value = new FieldValueUser(field());
            when(lookuper.lookupFieldValue(factoryItem, FIELD_ID)).thenReturn(value);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }
}
