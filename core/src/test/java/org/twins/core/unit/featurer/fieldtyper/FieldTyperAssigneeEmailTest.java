package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldTyperAssigneeEmailTest extends BaseUnitTest {

    private final FieldTyperAssigneeEmail fieldTyper = new FieldTyperAssigneeEmail();

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_returnsAssignerUserEmail() throws ServiceException {
            // Intended: the field renders the assignee's email as text.
            var email = "assignee@example.com";
            var user = new UserEntity().setId(UUID.randomUUID()).setEmail(email);
            var twin = new TwinEntity().setId(UUID.randomUUID()).setAssignerUser(user);
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValueText result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertEquals(email, result.getValue());
        }

        @Test
        void deserializeValue_nullAssignerEmail_returnsNullText() throws ServiceException {
            // Intended: when the assignee has no email, the text value is null (no NPE).
            var user = new UserEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID()).setAssignerUser(user);
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValueText result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertNull(result.getValue());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsImmutableDescriptor() throws ServiceException {
            // Intended: read-only derived field produces a bare FieldDescriptorImmutable.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            var descriptor = fieldTyper.getFieldDescriptor(classField, new Properties());

            assertInstanceOf(FieldDescriptorImmutable.class, descriptor);
        }
    }

    @Nested
    class CanSerialize {

        @Test
        void canSerialize_isFalse_assigneeEmailIsReadOnly() throws ServiceException {
            // Intended: assignee email is derived from the twin's assignee; clients must not write it directly.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            assertFalse(fieldTyper.canSerialize(classField));
        }
    }
}
