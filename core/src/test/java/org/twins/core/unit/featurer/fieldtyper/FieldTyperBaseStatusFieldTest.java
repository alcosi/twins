package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.value.FieldValueStatus;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldTyperBaseStatusFieldTest extends BaseUnitTest {

    private final FieldTyperBaseStatusField fieldTyper = new FieldTyperBaseStatusField();

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_returnsTwinStatus() throws ServiceException {
            // Intended: BaseStatus reads twin.getTwinStatus() into a status field value.
            var status = new TwinStatusEntity();
            var twin = new TwinEntity().setId(UUID.randomUUID()).setTwinStatus(status);
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValueStatus result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertSame(status, result.getValue());
        }

        @Test
        void deserializeValue_nullStatus_returnsNull() throws ServiceException {
            // Intended: a twin with no status yields a present field value whose value is null.
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValueStatus result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertNull(result.getValue());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsImmutableDescriptor() throws ServiceException {
            // Intended: BaseStatus is immutable from the typer's perspective.
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, new Properties());

            assertInstanceOf(FieldDescriptorImmutable.class, descriptor);
        }
    }
}
