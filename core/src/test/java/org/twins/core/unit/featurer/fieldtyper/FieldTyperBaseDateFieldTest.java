package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.twins.core.service.SystemEntityService.TWIN_CLASS_FIELD_TWIN_CREATED_AT;

class FieldTyperBaseDateFieldTest extends BaseUnitTest {

    private final FieldTyperBaseDateField fieldTyper = new FieldTyperBaseDateField();

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_createdAtField_returnsTwinCreatedAt() throws ServiceException {
            // Intended: the CREATED_AT system field reads twin.getCreatedAt() as a date value.
            var ldt = LocalDateTime.of(2026, 6, 21, 10, 15, 30);
            var twin = new TwinEntity()
                    .setId(UUID.randomUUID())
                    .setCreatedAt(Timestamp.valueOf(ldt));
            var classField = new TwinClassFieldEntity().setId(TWIN_CLASS_FIELD_TWIN_CREATED_AT);

            FieldValueDate result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertEquals(ldt, result.getDate());
        }

        @Test
        void deserializeValue_unknownField_throws() {
            // Intended: only CREATED_AT is supported; any other field id is incorrect.
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
        void getFieldDescriptor_returnsImmutableDescriptor() throws ServiceException {
            // Intended: BaseDate is immutable from the typer's perspective.
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, new Properties());

            assertInstanceOf(FieldDescriptorImmutable.class, descriptor);
        }
    }
}
