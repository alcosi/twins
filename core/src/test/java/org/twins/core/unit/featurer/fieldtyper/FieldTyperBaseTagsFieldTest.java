package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldTyperBaseTagsFieldTest extends BaseUnitTest {

    private final FieldTyperBaseTagsField fieldTyper = new FieldTyperBaseTagsField();

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_returnsTwinTags() throws ServiceException {
            // Intended: BaseTags reads twin.getTwinTagKit().getList() into a select field value.
            var tag1 = new DataListOptionEntity().setId(UUID.randomUUID());
            var tag2 = new DataListOptionEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity()
                    .setId(UUID.randomUUID())
                    .setTwinTagKit(new Kit<>(DataListOptionEntity::getId));
            twin.getTwinTagKit().add(tag1);
            twin.getTwinTagKit().add(tag2);
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValueSelect result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertEquals(List.of(tag1, tag2), result.getItems());
        }

        @Test
        void deserializeValue_emptyTags_returnsEmptyItems() throws ServiceException {
            // Intended: no tags -> empty list of items.
            var twin = new TwinEntity()
                    .setId(UUID.randomUUID())
                    .setTwinTagKit(new Kit<>(DataListOptionEntity::getId));
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValueSelect result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertTrue(result.getItems().isEmpty());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsImmutableDescriptor() throws ServiceException {
            // Intended: BaseTags is immutable from the typer's perspective.
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, new Properties());

            assertInstanceOf(FieldDescriptorImmutable.class, descriptor);
        }
    }
}
