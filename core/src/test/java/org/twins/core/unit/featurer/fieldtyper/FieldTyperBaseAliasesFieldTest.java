package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinAliasEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.value.FieldValueAliases;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldTyperBaseAliasesFieldTest extends BaseUnitTest {

    private final FieldTyperBaseAliasesField fieldTyper = new FieldTyperBaseAliasesField();

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_returnsTwinAliases() throws ServiceException {
            // Intended: BaseAliases reads twin.getTwinAliases().getList() into the field-value collection.
            var alias1 = new TwinAliasEntity().setId(UUID.randomUUID());
            var alias2 = new TwinAliasEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity()
                    .setId(UUID.randomUUID())
                    .setTwinAliases(new Kit<>(TwinAliasEntity::getAliasTypeId));
            twin.getTwinAliases().add(alias1);
            twin.getTwinAliases().add(alias2);
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValueAliases result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertEquals(List.of(alias1, alias2), result.getItems());
        }

        @Test
        void deserializeValue_emptyAliases_returnsEmptyItems() throws ServiceException {
            // Intended: no aliases -> empty list of items (kit present, zero entries).
            var twin = new TwinEntity()
                    .setId(UUID.randomUUID())
                    .setTwinAliases(new Kit<>(TwinAliasEntity::getAliasTypeId));
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValueAliases result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertTrue(result.getItems().isEmpty());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsImmutableDescriptor() throws ServiceException {
            // Intended: BaseAliases is immutable from the typer's perspective.
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, new Properties());

            assertInstanceOf(FieldDescriptorImmutable.class, descriptor);
        }
    }
}
