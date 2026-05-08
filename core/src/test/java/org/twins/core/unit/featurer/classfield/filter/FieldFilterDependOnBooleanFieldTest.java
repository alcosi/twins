package org.twins.core.featurer.classfield.filter;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldBooleanEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.service.twin.TwinService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


class FieldFilterDependOnBooleanFieldTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private FieldFilterDependOnBooleanField filter;

    private UUID booleanFieldId;
    private UUID fieldId1;
    private UUID fieldId2;

    @BeforeEach
    void setUp() {
        filter = new FieldFilterDependOnBooleanField(twinService);
        booleanFieldId = UUID.randomUUID();
        fieldId1 = UUID.randomUUID();
        fieldId2 = UUID.randomUUID();
    }

    private Properties props(UUID booleanFieldUuid, boolean excludeOnTrue) {
        var props = new Properties();
        props.put("booleanFieldId", booleanFieldUuid.toString());
        props.put("excludeOnTrue", String.valueOf(excludeOnTrue));
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

    private List<TwinClassFieldEntity> twoFields() {
        var list = new ArrayList<TwinClassFieldEntity>();
        list.add(fieldEntity(fieldId1));
        list.add(fieldEntity(fieldId2));
        return list;
    }

    private Kit<TwinFieldBooleanEntity, UUID> booleanKit(UUID twinClassFieldId, Boolean value) {
        var entity = new TwinFieldBooleanEntity();
        entity.setTwinClassFieldId(twinClassFieldId);
        entity.setValue(value);
        var kit = new Kit<TwinFieldBooleanEntity, UUID>(TwinFieldBooleanEntity::getTwinClassFieldId);
        kit.add(entity);
        return kit;
    }

    @Nested
    class ExcludeOnTrue {

        @Test
        void filterFields_booleanTrue_addsFields() throws ServiceException {
            var twin = new TwinEntity();
            twin.setTwinFieldBooleanKit(booleanKit(booleanFieldId, true));

            doNothing().when(twinService).loadTwinFields(twin);

            var unfilteredFieldsKit = new Kit<TwinClassFieldEntity, UUID>(TwinClassFieldEntity::getId);

            filter.filterFields(
                    props(booleanFieldId, true),
                    unfilteredFieldsKit,
                    twin,
                    twoFields()
            );

            assertEquals(2, unfilteredFieldsKit.size());
        }

        @Test
        void filterFields_booleanFalse_doesNotAddFields() throws ServiceException {
            var twin = new TwinEntity();
            twin.setTwinFieldBooleanKit(booleanKit(booleanFieldId, false));

            doNothing().when(twinService).loadTwinFields(twin);

            var unfilteredFieldsKit = new Kit<TwinClassFieldEntity, UUID>(TwinClassFieldEntity::getId);

            filter.filterFields(
                    props(booleanFieldId, true),
                    unfilteredFieldsKit,
                    twin,
                    twoFields()
            );

            assertTrue(unfilteredFieldsKit.isEmpty());
        }

        @Test
        void filterFields_booleanFieldNotFound_doesNotAddFields() throws ServiceException {
            var twin = new TwinEntity();
            var kit = new Kit<TwinFieldBooleanEntity, UUID>(TwinFieldBooleanEntity::getTwinClassFieldId);
            twin.setTwinFieldBooleanKit(kit);

            doNothing().when(twinService).loadTwinFields(twin);

            var unfilteredFieldsKit = new Kit<TwinClassFieldEntity, UUID>(TwinClassFieldEntity::getId);

            filter.filterFields(
                    props(booleanFieldId, true),
                    unfilteredFieldsKit,
                    twin,
                    twoFields()
            );

            assertTrue(unfilteredFieldsKit.isEmpty());
        }
    }

    @Nested
    class ExcludeOnFalse {

        @Test
        void filterFields_booleanFalse_addsFields() throws ServiceException {
            var twin = new TwinEntity();
            twin.setTwinFieldBooleanKit(booleanKit(booleanFieldId, false));

            doNothing().when(twinService).loadTwinFields(twin);

            var unfilteredFieldsKit = new Kit<TwinClassFieldEntity, UUID>(TwinClassFieldEntity::getId);

            filter.filterFields(
                    props(booleanFieldId, false),
                    unfilteredFieldsKit,
                    twin,
                    twoFields()
            );

            assertEquals(2, unfilteredFieldsKit.size());
        }

        @Test
        void filterFields_booleanTrue_doesNotAddFields() throws ServiceException {
            var twin = new TwinEntity();
            twin.setTwinFieldBooleanKit(booleanKit(booleanFieldId, true));

            doNothing().when(twinService).loadTwinFields(twin);

            var unfilteredFieldsKit = new Kit<TwinClassFieldEntity, UUID>(TwinClassFieldEntity::getId);

            filter.filterFields(
                    props(booleanFieldId, false),
                    unfilteredFieldsKit,
                    twin,
                    twoFields()
            );

            assertTrue(unfilteredFieldsKit.isEmpty());
        }
    }

    @Nested
    class EmptyFields {

        @Test
        void filterFields_emptyFieldsList_nothingAdded() throws ServiceException {
            var twin = new TwinEntity();
            twin.setTwinFieldBooleanKit(booleanKit(booleanFieldId, true));

            doNothing().when(twinService).loadTwinFields(twin);

            var unfilteredFieldsKit = new Kit<TwinClassFieldEntity, UUID>(TwinClassFieldEntity::getId);

            filter.filterFields(
                    props(booleanFieldId, true),
                    unfilteredFieldsKit,
                    twin,
                    List.of()
            );

            assertTrue(unfilteredFieldsKit.isEmpty());
        }
    }

}
