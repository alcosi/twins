package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorAttachment;
import org.twins.core.featurer.fieldtyper.value.FieldValueInvisible;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldTyperAttachmentInvisibleTest extends BaseUnitTest {

    private FieldTyperAttachmentInvisible fieldTyper;

    @BeforeEach
    void setUp() {
        fieldTyper = new FieldTyperAttachmentInvisible();
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    private Properties properties() {
        return new Properties();
    }

    @Nested
    class CanSerialize {

        @Test
        void canSerialize_returnsFalse() throws ServiceException {
            // Intended: the invisible attachment typer is read-only by design -> canSerialize is false.
            var classField = new TwinClassFieldEntity();

            assertFalse(fieldTyper.canSerialize(classField));
        }
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_isNoOp() throws ServiceException {
            // Intended: serialize is a deprecated no-op; it must not touch the collector.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueInvisible(classField);
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, value, collector);

            assertFalse(collector.hasChanges());
        }
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_returnsEmptyInvisibleValue() throws ServiceException {
            // Intended: deserialize yields a fresh FieldValueInvisible carrying no payload.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());

            FieldValueInvisible result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertNotNull(result);
            assertTrue(result.isUndefined());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_noRestriction_returnsPlainAttachmentDescriptor() throws ServiceException {
            // Intended: with no restrictionId, the base descriptor is returned (counts/restrictions null).
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, properties());

            assertInstanceOf(FieldDescriptorAttachment.class, descriptor);
            var attachment = (FieldDescriptorAttachment) descriptor;
            assertNull(attachment.minCount());
            assertNull(attachment.maxCount());
        }
    }
}
