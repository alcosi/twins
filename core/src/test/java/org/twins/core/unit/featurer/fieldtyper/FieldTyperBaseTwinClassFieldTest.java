package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.value.FieldValueTwinClassSingle;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldTyperBaseTwinClassFieldTest extends BaseUnitTest {

    private final FieldTyperBaseTwinClassField fieldTyper = new FieldTyperBaseTwinClassField();

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_returnsTwinClass() throws ServiceException {
            // Intended: BaseTwinClass reads twin.getTwinClass() into a single twin-class field value.
            var twinClass = new TwinClassEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID()).setTwinClass(twinClass);
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValueTwinClassSingle result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertSame(twinClass, result.getValue());
        }

        @Test
        void deserializeValue_nullTwinClass_returnsNull() throws ServiceException {
            // Intended: a twin with no class yields a present field value whose value is null.
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValueTwinClassSingle result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertNull(result.getValue());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsImmutableDescriptor() throws ServiceException {
            // Intended: BaseTwinClass is immutable from the typer's perspective.
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, new Properties());

            assertInstanceOf(FieldDescriptorImmutable.class, descriptor);
        }
    }
}
