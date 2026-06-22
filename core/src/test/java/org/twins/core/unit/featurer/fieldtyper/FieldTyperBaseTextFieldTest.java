package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.twins.core.service.SystemEntityService.TWIN_CLASS_FIELD_TWIN_DESCRIPTION;
import static org.twins.core.service.SystemEntityService.TWIN_CLASS_FIELD_TWIN_EXTERNAL_ID;
import static org.twins.core.service.SystemEntityService.TWIN_CLASS_FIELD_TWIN_NAME;

class FieldTyperBaseTextFieldTest extends BaseUnitTest {

    private final FieldTyperBaseTextField fieldTyper = new FieldTyperBaseTextField();

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_nameField_returnsTwinName() throws ServiceException {
            // Intended: the NAME system field reads twin.getName().
            var twin = new TwinEntity().setId(UUID.randomUUID()).setName("alpha");
            var classField = new TwinClassFieldEntity().setId(TWIN_CLASS_FIELD_TWIN_NAME);

            FieldValueText result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertEquals("alpha", result.getValue());
        }

        @Test
        void deserializeValue_descriptionField_returnsTwinDescription() throws ServiceException {
            // Intended: the DESCRIPTION system field reads twin.getDescription().
            var twin = new TwinEntity().setId(UUID.randomUUID()).setDescription("desc");
            var classField = new TwinClassFieldEntity().setId(TWIN_CLASS_FIELD_TWIN_DESCRIPTION);

            FieldValueText result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertEquals("desc", result.getValue());
        }

        @Test
        void deserializeValue_externalIdField_returnsTwinExternalId() throws ServiceException {
            // Intended: the EXTERNAL_ID system field reads twin.getExternalId().
            var twin = new TwinEntity().setId(UUID.randomUUID()).setExternalId("ext-1");
            var classField = new TwinClassFieldEntity().setId(TWIN_CLASS_FIELD_TWIN_EXTERNAL_ID);

            FieldValueText result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertEquals("ext-1", result.getValue());
        }

        @Test
        void deserializeValue_unknownField_throws() {
            // Intended: only NAME/DESCRIPTION/EXTERNAL_ID are supported; anything else is incorrect.
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            var ex = assertThrows(ServiceException.class,
                    () -> fieldTyper.deserializeValue(new Properties(), twinField(twin, classField)));

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT.getCode(), ex.getErrorCode());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsTextDescriptor() throws ServiceException {
            // Intended: BaseText exposes a text descriptor.
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, new Properties());

            assertInstanceOf(FieldDescriptorText.class, descriptor);
        }
    }
}
