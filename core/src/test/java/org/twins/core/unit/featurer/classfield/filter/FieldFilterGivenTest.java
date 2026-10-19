package org.twins.core.featurer.classfield.filter;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class FieldFilterGivenTest extends BaseUnitTest {

    private final FieldFilterGiven filter = new FieldFilterGiven();

    private UUID fieldId1;
    private UUID fieldId2;
    private UUID fieldId3;

    @BeforeEach
    void setUp() {
        fieldId1 = UUID.randomUUID();
        fieldId2 = UUID.randomUUID();
        fieldId3 = UUID.randomUUID();
    }

    private Properties props(String filteredFieldIds) {
        var props = new Properties();
        if (filteredFieldIds != null)
            props.put("filteredFieldIds", filteredFieldIds);
        return props;
    }

    private TwinClassFieldEntity fieldEntity(UUID id) {
        var entity = new TwinClassFieldEntity();
        try {
            var idField = TwinClassFieldEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            fail("Failed to set id on TwinClassFieldEntity: " + e.getMessage());
        }
        return entity;
    }

    private List<TwinClassFieldEntity> threeFields() {
        var list = new ArrayList<TwinClassFieldEntity>();
        list.add(fieldEntity(fieldId1));
        list.add(fieldEntity(fieldId2));
        list.add(fieldEntity(fieldId3));
        return list;
    }

    @Nested
    class FilterFields {

        @Test
        void filterFields_excludesGivenIds() throws ServiceException {
            var unfilteredFieldsKit = new Kit<>(TwinClassFieldEntity::getId);

            filter.filterFields(
                    props(fieldId1 + ", " + fieldId3),
                    unfilteredFieldsKit,
                    new TwinEntity(),
                    threeFields()
            );

            assertEquals(1, unfilteredFieldsKit.size());
            assertTrue(unfilteredFieldsKit.containsKey(fieldId2));
        }

        @Test
        void filterFields_excludesAllIds_returnsEmpty() throws ServiceException {
            var unfilteredFieldsKit = new Kit<>(TwinClassFieldEntity::getId);

            filter.filterFields(
                    props(fieldId1 + ", " + fieldId2 + ", " + fieldId3),
                    unfilteredFieldsKit,
                    new TwinEntity(),
                    threeFields()
            );

            assertTrue(unfilteredFieldsKit.isEmpty());
        }

        @Test
        void filterFields_emptyFilterSet_keepsAllFields() throws ServiceException {
            var unfilteredFieldsKit = new Kit<>(TwinClassFieldEntity::getId);

            filter.filterFields(
                    props(""),
                    unfilteredFieldsKit,
                    new TwinEntity(),
                    threeFields()
            );

            assertEquals(3, unfilteredFieldsKit.size());
        }

        @Test
        void filterFields_singleExcludedId_keepsOthers() throws ServiceException {
            var unfilteredFieldsKit = new Kit<>(TwinClassFieldEntity::getId);

            filter.filterFields(
                    props(fieldId2.toString()),
                    unfilteredFieldsKit,
                    new TwinEntity(),
                    threeFields()
            );

            assertEquals(2, unfilteredFieldsKit.size());
            assertTrue(unfilteredFieldsKit.containsKey(fieldId1));
            assertTrue(unfilteredFieldsKit.containsKey(fieldId3));
        }

        @Test
        void filterFields_noMatchingIds_keepsAllFields() throws ServiceException {
            var unfilteredFieldsKit = new Kit<>(TwinClassFieldEntity::getId);
            var unknownId = UUID.randomUUID();

            filter.filterFields(
                    props(unknownId.toString()),
                    unfilteredFieldsKit,
                    new TwinEntity(),
                    threeFields()
            );

            assertEquals(3, unfilteredFieldsKit.size());
        }

        @Test
        void filterFields_emptyFieldsList_returnsEmpty() throws ServiceException {
            var unfilteredFieldsKit = new Kit<>(TwinClassFieldEntity::getId);

            filter.filterFields(
                    props(fieldId1.toString()),
                    unfilteredFieldsKit,
                    new TwinEntity(),
                    List.of()
            );

            assertTrue(unfilteredFieldsKit.isEmpty());
        }
    }
}
