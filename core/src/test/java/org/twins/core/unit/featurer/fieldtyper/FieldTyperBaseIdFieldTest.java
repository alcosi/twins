package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.value.FieldValueId;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldTyperBaseIdFieldTest extends BaseUnitTest {

    private final FieldTyperBaseIdField fieldTyper = new FieldTyperBaseIdField();

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_returnsTwinId() throws ServiceException {
            // Intended: BaseId field typer reads twin.getId() and wraps it in FieldValueId.
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity().setId(twinId);
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValueId result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertEquals(twinId, result.getValue());
        }

        @Test
        void deserializeValue_nullTwinId_returnsNull() throws ServiceException {
            // Intended: a twin without an id yields a present FieldValue whose value is null.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValueId result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertNull(result.getValue());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsImmutableDescriptor() throws ServiceException {
            // Intended: BaseId exposes an immutable descriptor (cannot be edited through this typer).
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, new Properties());

            assertInstanceOf(FieldDescriptorImmutable.class, descriptor);
        }
    }
}
