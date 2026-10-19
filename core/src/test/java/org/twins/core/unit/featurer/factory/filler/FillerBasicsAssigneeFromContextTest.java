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
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromContext;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FillerBasicsAssigneeFromContextTest extends BaseUnitTest {

    private FillerBasicsAssigneeFromContext filler;

    private static final UUID ASSIGNEE_FIELD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        filler = new FillerBasicsAssigneeFromContext();
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

            filler.fill(props(), factoryItem, null, fieldValue);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertSame(user, outputTwin.getAssignerUser());
            assertEquals(user.getId(), outputTwin.getAssignerUserId());
        }

        @Test
        void fill_undefinedUserField_throwsStepError() throws ServiceException {
            // the new lookuper contract: a not-found lookup arrives as an undefined value, and this
            // filler has no default for that scenario, so the item fails itself.
            var factoryItem = buildFactoryItem();
            var fieldValue = new FieldValueUser(buildField()); // undefined

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), factoryItem, null, fieldValue));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_multipleUsers_throwsMultipleNotAllowed() throws ServiceException {
            var factoryItem = buildFactoryItem();
            var fieldValue = new FieldValueUser(buildField())
                    .add(new UserEntity().setId(UUID.randomUUID()))
                    .add(new UserEntity().setId(UUID.randomUUID()));

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), factoryItem, null, fieldValue));
            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_nonUserField_throwsIncorrectType() throws ServiceException {
            var factoryItem = buildFactoryItem();
            var fieldValue = new FieldValueText(buildField()).setValue("not-a-user"); // defined — the type check is what fails

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), factoryItem, null, fieldValue));
            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE.getCode(), ex.getErrorCode());
        }
    }
}
