package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorListShared;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.datalist.DataListService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldTyperSharedSelectInHeadTest extends BaseUnitTest {

    @Mock
    private DataListService dataListService;

    @Mock
    private DataListOptionService dataListOptionService;

    private FieldTyperSharedSelectInHead fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperSharedSelectInHead();
        setField(fieldTyper, "dataListService", dataListService);
        setField(fieldTyper, "dataListOptionService", dataListOptionService);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new RuntimeException("Field not found: " + fieldName);
    }

    private Properties properties(UUID listId) {
        var props = new Properties();
        props.setProperty("listUUID", listId.toString());
        return props;
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsSharedDescriptorNotMultiple() throws ServiceException {
            // Intended: the head-scoped shared select yields a FieldDescriptorListShared flagged single-valued.
            var listId = UUID.randomUUID();
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            when(dataListService.findEntitySafe(listId)).thenReturn(new DataListEntity().setId(listId));

            var descriptor = fieldTyper.getFieldDescriptor(classField, properties(listId));

            assertInstanceOf(FieldDescriptorListShared.class, descriptor);
            assertFalse(((FieldDescriptorListShared) descriptor).isMultiple());
        }
    }

    @Nested
    class CheckOptionAllowed {

        @Test
        void checkOptionAllowed_optionInUse_throws() {
            // Intended: an option already taken by another twin under the same head must be rejected.
            var listId = UUID.randomUUID();
            var classFieldId = UUID.randomUUID();
            var headTwinId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var classField = new TwinClassFieldEntity().setId(classFieldId);
            var option = new DataListOptionEntity().setId(UUID.randomUUID()).setDataListId(listId);
            var twin = new TwinEntity().setId(twinId).setHeadTwinId(headTwinId);
            when(dataListService.findOptionIdsByDataListIdAndNotUsedInHeadExcludingTwin(listId, classFieldId, headTwinId, twinId))
                    .thenReturn(Set.of());

            var ex = assertThrows(ServiceException.class,
                    () -> fieldTyper.checkOptionAllowed(twin, classField, option));

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_IS_ALREADY_IN_USE.getCode(), ex.getErrorCode());
        }

        @Test
        void checkOptionAllowed_optionFree_returnsOptionId() throws ServiceException {
            // Intended: an option id present in the free set is allowed and returned.
            var listId = UUID.randomUUID();
            var classFieldId = UUID.randomUUID();
            var headTwinId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var classField = new TwinClassFieldEntity().setId(classFieldId);
            var option = new DataListOptionEntity().setId(UUID.randomUUID()).setDataListId(listId);
            var twin = new TwinEntity().setId(twinId).setHeadTwinId(headTwinId);
            when(dataListService.findOptionIdsByDataListIdAndNotUsedInHeadExcludingTwin(listId, classFieldId, headTwinId, twinId))
                    .thenReturn(Set.of(option.getId()));

            assertEquals(option.getId(), fieldTyper.checkOptionAllowed(twin, classField, option));
        }
    }

    @Nested
    class AllowMultiply {

        @Test
        void allowMultiply_alwaysFalse_sharedSelectIsSingleValued() throws ServiceException {
            // Intended: a shared select is single-valued regardless of params.
            assertFalse(fieldTyper.allowMultiply(properties(UUID.randomUUID())));
        }
    }
}
