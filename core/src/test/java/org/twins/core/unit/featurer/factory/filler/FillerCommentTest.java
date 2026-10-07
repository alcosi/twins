package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerComment;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FillerCommentTest extends BaseUnitTest {

    private FillerComment filler;

    private static final UUID FIELD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        filler = new FillerComment();
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

            filler.fill(props(), factoryItem, null, value);

            var create = (TwinCreate) factoryItem.getOutput();
            assertNotNull(create.getCommentsAdd());
            assertTrue(create.getCommentsAdd().contains("hello world"));
        }

        @Test
        void fill_nonTextField_throwsStepError() {
            var factoryItem = buildFactoryItem();
            FieldValue value = new FieldValueUser(field());

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), factoryItem, null, value));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }
}
