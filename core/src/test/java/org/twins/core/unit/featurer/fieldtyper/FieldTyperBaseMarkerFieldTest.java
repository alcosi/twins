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

class FieldTyperBaseMarkerFieldTest extends BaseUnitTest {

    private final FieldTyperBaseMarkerField fieldTyper = new FieldTyperBaseMarkerField();

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_returnsTwinMarkers() throws ServiceException {
            // Intended: BaseMarkers reads twin.getTwinMarkerKit().getList() into a select field value.
            var marker1 = new DataListOptionEntity().setId(UUID.randomUUID());
            var marker2 = new DataListOptionEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity()
                    .setId(UUID.randomUUID())
                    .setTwinMarkerKit(new Kit<>(DataListOptionEntity::getId));
            twin.getTwinMarkerKit().add(marker1);
            twin.getTwinMarkerKit().add(marker2);
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValueSelect result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertEquals(List.of(marker1, marker2), result.getItems());
        }

        @Test
        void deserializeValue_emptyMarkers_returnsEmptyItems() throws ServiceException {
            // Intended: no markers -> empty list of items.
            var twin = new TwinEntity()
                    .setId(UUID.randomUUID())
                    .setTwinMarkerKit(new Kit<>(DataListOptionEntity::getId));
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValueSelect result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertTrue(result.getItems().isEmpty());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsImmutableDescriptor() throws ServiceException {
            // Intended: BaseMarkers is immutable from the typer's perspective.
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, new Properties());

            assertInstanceOf(FieldDescriptorImmutable.class, descriptor);
        }
    }
}
