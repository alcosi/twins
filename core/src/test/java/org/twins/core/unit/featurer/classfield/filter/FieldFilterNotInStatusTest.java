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


class FieldFilterNotInStatusTest extends BaseUnitTest {

    private final FieldFilterNotInStatus filter = new FieldFilterNotInStatus();

    private UUID statusId1;
    private UUID statusId2;
    private UUID fieldId1;
    private UUID fieldId2;

    @BeforeEach
    void setUp() {
        statusId1 = UUID.randomUUID();
        statusId2 = UUID.randomUUID();
        fieldId1 = UUID.randomUUID();
        fieldId2 = UUID.randomUUID();
    }

    private Properties props(String statusIds) {
        var props = new Properties();
        if (statusIds != null)
            props.put("statusIds", statusIds);
        return props;
    }

    private TwinEntity twinWithStatus(UUID statusId) {
        var twin = new TwinEntity();
        twin.setTwinStatusId(statusId);
        return twin;
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

    private List<TwinClassFieldEntity> twoFields() {
        var list = new ArrayList<TwinClassFieldEntity>();
        list.add(fieldEntity(fieldId1));
        list.add(fieldEntity(fieldId2));
        return list;
    }

    @Nested
    class FilterFields {

        @Test
        void filterFields_twinStatusInSet_addsFields() throws ServiceException {
            var unfilteredFieldsKit = new Kit<>(TwinClassFieldEntity::getId);

            filter.filterFields(
                    props(statusId1 + ", " + statusId2),
                    unfilteredFieldsKit,
                    twinWithStatus(statusId1),
                    twoFields()
            );

            assertEquals(2, unfilteredFieldsKit.size());
        }

        @Test
        void filterFields_twinStatusNotInSet_doesNotAddFields() throws ServiceException {
            var unfilteredFieldsKit = new Kit<>(TwinClassFieldEntity::getId);

            filter.filterFields(
                    props(statusId1 + ", " + statusId2),
                    unfilteredFieldsKit,
                    twinWithStatus(UUID.randomUUID()),
                    twoFields()
            );

            assertTrue(unfilteredFieldsKit.isEmpty());
        }

        @Test
        void filterFields_twinStatusNull_doesNotAddFields() throws ServiceException {
            var unfilteredFieldsKit = new Kit<>(TwinClassFieldEntity::getId);

            filter.filterFields(
                    props(statusId1 + ", " + statusId2),
                    unfilteredFieldsKit,
                    new TwinEntity(),
                    twoFields()
            );

            assertTrue(unfilteredFieldsKit.isEmpty());
        }

        @Test
        void filterFields_emptyStatusSet_doesNotAddFields() throws ServiceException {
            var unfilteredFieldsKit = new Kit<>(TwinClassFieldEntity::getId);

            filter.filterFields(
                    props(""),
                    unfilteredFieldsKit,
                    twinWithStatus(statusId1),
                    twoFields()
            );

            assertTrue(unfilteredFieldsKit.isEmpty());
        }

        @Test
        void filterFields_singleStatus_twinMatches_addsFields() throws ServiceException {
            var unfilteredFieldsKit = new Kit<>(TwinClassFieldEntity::getId);

            filter.filterFields(
                    props(statusId1.toString()),
                    unfilteredFieldsKit,
                    twinWithStatus(statusId1),
                    twoFields()
            );

            assertEquals(2, unfilteredFieldsKit.size());
        }

        @Test
        void filterFields_emptyFieldsList_nothingAdded() throws ServiceException {
            var unfilteredFieldsKit = new Kit<>(TwinClassFieldEntity::getId);

            filter.filterFields(
                    props(statusId1.toString()),
                    unfilteredFieldsKit,
                    twinWithStatus(statusId1),
                    List.of()
            );

            assertTrue(unfilteredFieldsKit.isEmpty());
        }
    }
}
