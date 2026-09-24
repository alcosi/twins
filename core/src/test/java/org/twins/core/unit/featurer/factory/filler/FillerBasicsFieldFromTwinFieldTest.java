package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerBasicsFieldFromTwinField;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FillerBasicsFieldFromTwinFieldTest extends BaseUnitTest {

    private FillerBasicsFieldFromTwinField filler;

    @BeforeEach
    void setUp() {
        filler = new FillerBasicsFieldFromTwinField();
    }

    private Properties props(UUID fieldId) {
        var p = new Properties();
        p.setProperty("fieldId", fieldId.toString());
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

    private TwinClassFieldEntity buildField(UUID id) {
        var field = new TwinClassFieldEntity();
        field.setId(id);
        field.setKey("field");
        return field;
    }

    @Nested
    class Fill {

        @Test
        void fill_nameField_setsOutputName() throws ServiceException {
            var fieldId = SystemIds.TwinClassField.Base.NAME;
            var factoryItem = buildFactoryItem();
            var fieldValue = new FieldValueText(buildField(fieldId)).setValue("my name");

            filler.fill(props(fieldId), factoryItem, null, fieldValue);

            assertEquals("my name", factoryItem.getOutput().getTwinEntity().getName());
        }

        @Test
        void fill_descriptionField_setsOutputDescription() throws ServiceException {
            var fieldId = SystemIds.TwinClassField.Base.DESCRIPTION;
            var factoryItem = buildFactoryItem();
            var fieldValue = new FieldValueText(buildField(fieldId)).setValue("a description");

            filler.fill(props(fieldId), factoryItem, null, fieldValue);

            assertEquals("a description", factoryItem.getOutput().getTwinEntity().getDescription());
        }

        @Test
        void fill_assigneeUserField_setsOutputAssignee() throws ServiceException {
            var fieldId = SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID;
            var factoryItem = buildFactoryItem();
            var user = new UserEntity().setId(UUID.randomUUID());
            var fieldValue = new FieldValueUser(buildField(fieldId)).add(user);

            filler.fill(props(fieldId), factoryItem, null, fieldValue);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertSame(user, outputTwin.getAssignerUser());
            assertEquals(user.getId(), outputTwin.getAssignerUserId());
        }

        @Test
        void fill_creatorUserField_setsOutputCreatedBy() throws ServiceException {
            var fieldId = SystemIds.TwinClassField.Base.CREATOR_USER_ID;
            var factoryItem = buildFactoryItem();
            var user = new UserEntity().setId(UUID.randomUUID());
            var fieldValue = new FieldValueUser(buildField(fieldId)).add(user);

            filler.fill(props(fieldId), factoryItem, null, fieldValue);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            // NAME/contract: creator user maps to createdBy on the output twin.
            assertSame(user, outputTwin.getCreatedByUser());
            assertEquals(user.getId(), outputTwin.getCreatedByUserId());
        }

        @Test
        void fill_emptyUserField_throwsRequired() throws ServiceException {
            var fieldId = SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID;
            var factoryItem = buildFactoryItem();
            var fieldValue = new FieldValueUser(buildField(fieldId)); // undefined -> empty

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(fieldId), factoryItem, null, fieldValue));
            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED.getCode(), ex.getErrorCode());
        }
    }
}
