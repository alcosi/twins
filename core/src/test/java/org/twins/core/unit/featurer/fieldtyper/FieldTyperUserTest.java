package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldUserEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorUser;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserFilterService;
import org.twins.core.service.user.UserService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldTyperUserTest extends BaseUnitTest {

    @Mock
    private UserFilterService userFilterService;

    @Mock
    private UserService userService;

    @Mock
    private TwinService twinService;

    private FieldTyperUser fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperUser();
        setField(fieldTyper, "userFilterService", userFilterService);
        setField(fieldTyper, "userService", userService);
        setField(fieldTyper, "twinService", twinService);
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

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    private Properties properties(UUID filterId, String multiple, String longListThreshold) {
        var props = new Properties();
        props.setProperty("userFilterUUID", filterId.toString());
        props.setProperty("multiple", multiple);
        props.setProperty("longListThreshold", longListThreshold);
        return props;
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_shortList_eagerlyLoadsValidUsers() throws ServiceException {
            // Intended: when the filter result fits under the long-list threshold, the users are loaded inline.
            var filterId = UUID.randomUUID();
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var u1 = new UserEntity().setId(UUID.randomUUID());
            when(userFilterService.countFilterResult(filterId)).thenReturn(1);
            when(userFilterService.findUsers(filterId)).thenReturn(List.of(u1));

            var descriptor = (FieldDescriptorUser) fieldTyper.getFieldDescriptor(classField, properties(filterId, "true", "10"));

            assertTrue(descriptor.multiple());
            assertEquals(1, descriptor.validUsers().size());
            assertSame(u1, descriptor.validUsers().get(0));
            assertNull(descriptor.userFilterId());
        }

        @Test
        void getFieldDescriptor_longList_exposesFilterIdInsteadOfUsers() throws ServiceException {
            // Intended: when the filter result exceeds the threshold, only the filter id is exposed (long list).
            var filterId = UUID.randomUUID();
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            when(userFilterService.countFilterResult(filterId)).thenReturn(100);

            var descriptor = (FieldDescriptorUser) fieldTyper.getFieldDescriptor(classField, properties(filterId, "false", "10"));

            assertFalse(descriptor.multiple());
            assertEquals(filterId, descriptor.userFilterId());
            assertTrue(descriptor.validUsers().isEmpty());
        }
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_multipleUsersWhenMultipleFalse_throws() throws ServiceException {
            // Intended: a non-multi user field rejects more than one user before touching storage.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueUser(classField);
            value.add(new UserEntity().setId(UUID.randomUUID()));
            value.add(new UserEntity().setId(UUID.randomUUID()));

            var ex = assertThrows(ServiceException.class,
                    () -> fieldTyper.serializeValue(
                            properties(UUID.randomUUID(), "false", "0"),
                            twin, value, new TwinChangesCollector()));

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED.getCode(), ex.getErrorCode());
        }
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_loadsStoredUsersForField() throws ServiceException {
            // Intended: deserialization reads the twin's stored user entities for this field into the value.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var u1 = new UserEntity().setId(UUID.randomUUID());
            var u2 = new UserEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var stored1 = new TwinFieldUserEntity().setUser(u1).setUserId(u1.getId()).setTwinClassFieldId(classField.getId());
            var stored2 = new TwinFieldUserEntity().setUser(u2).setUserId(u2.getId()).setTwinClassFieldId(classField.getId());
            twin.setTwinFieldUserKit(new KitGrouped<>(List.of(stored1, stored2), TwinFieldUserEntity::getUserId, TwinFieldUserEntity::getTwinClassFieldId));

            FieldValueUser result = fieldTyper.deserializeValue(properties(UUID.randomUUID(), "true", "0"), twinField(twin, classField));

            // KitGrouped.getGrouped is backed by a HashMap → order is not guaranteed; assert membership.
            assertEquals(2, result.getItems().size());
            assertTrue(result.getItems().contains(u1));
            assertTrue(result.getItems().contains(u2));
        }

        @Test
        void deserializeValue_noStoredUsers_returnsEmptyValue() throws ServiceException {
            // Intended: a twin with no stored users for the field yields an empty (zero-item) value.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity()
                    .setId(UUID.randomUUID())
                    .setTwinFieldUserKit(KitGrouped.EMPTY);

            FieldValueUser result = fieldTyper.deserializeValue(properties(UUID.randomUUID(), "true", "0"), twinField(twin, classField));

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class AllowMultiply {

        @Test
        void allowMultiply_reflectsMultipleParam() throws ServiceException {
            // Intended: the "multiple" param drives whether more than one user is permitted.
            var filterId = UUID.randomUUID();
            assertTrue(fieldTyper.allowMultiply(properties(filterId, "true", "0")));
            assertFalse(fieldTyper.allowMultiply(properties(filterId, "false", "0")));
        }
    }
}
