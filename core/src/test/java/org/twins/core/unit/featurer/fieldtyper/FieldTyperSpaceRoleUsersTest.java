package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorUser;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.space.SpaceRoleUserService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserFilterService;
import org.twins.core.service.user.UserService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldTyperSpaceRoleUsersTest extends BaseUnitTest {

    @Mock
    private UserFilterService userFilterService;

    @Mock
    private UserService userService;

    @Mock
    private SpaceRoleUserService spaceRoleUserService;

    @Mock
    private AuthService authService;

    @Mock
    private TwinService twinService;

    private FieldTyperSpaceRoleUsers fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperSpaceRoleUsers();
        setField(fieldTyper, "userFilterService", userFilterService);
        setField(fieldTyper, "userService", userService);
        setField(fieldTyper, "spaceRoleUserService", spaceRoleUserService);
        setField(fieldTyper, "authService", authService);
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

    private Properties properties(UUID filterId, UUID roleId, String longListThreshold) {
        var props = new Properties();
        props.setProperty("userFilterUUID", filterId.toString());
        props.setProperty("spaceRoleId", roleId.toString());
        props.setProperty("longListThreshold", longListThreshold);
        return props;
    }

    private TwinClassFieldEntity classFieldOnSchemaSpaceTwinClass(boolean schemaSpace) {
        var twinClass = new TwinClassEntity().setPermissionSchemaSpace(schemaSpace);
        return new TwinClassFieldEntity()
                .setId(UUID.randomUUID())
                .setTwinClass(twinClass)
                .setTwinClassId(UUID.randomUUID());
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_alwaysMultipleAndEagerlyLoadsUsersForShortList() throws ServiceException {
            // Intended: space-role-users is inherently multi-valued (multiple=true hardcoded);
            // a short filter result is loaded inline.
            var filterId = UUID.randomUUID();
            var roleId = UUID.randomUUID();
            var classField = classFieldOnSchemaSpaceTwinClass(true);
            var u1 = new UserEntity().setId(UUID.randomUUID());
            when(userFilterService.countFilterResult(filterId)).thenReturn(1);
            when(userFilterService.findUsers(filterId)).thenReturn(List.of(u1));

            var descriptor = (FieldDescriptorUser) fieldTyper.getFieldDescriptor(classField, properties(filterId, roleId, "10"));

            assertTrue(descriptor.multiple());
            assertEquals(1, descriptor.validUsers().size());
            assertSame(u1, descriptor.validUsers().get(0));
        }

        @Test
        void getFieldDescriptor_longList_exposesFilterId() throws ServiceException {
            // Intended: above the threshold only the filter id is exposed.
            var filterId = UUID.randomUUID();
            var roleId = UUID.randomUUID();
            var classField = classFieldOnSchemaSpaceTwinClass(true);
            when(userFilterService.countFilterResult(filterId)).thenReturn(1000);

            var descriptor = (FieldDescriptorUser) fieldTyper.getFieldDescriptor(classField, properties(filterId, roleId, "10"));

            assertEquals(filterId, descriptor.userFilterId());
            assertTrue(descriptor.validUsers().isEmpty());
        }
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_twinClassWithoutPermissionSchemaSpace_isNoop() throws ServiceException {
            // Intended: space-role serialization only applies to twin classes that carry a permission-schema space;
            // otherwise the call returns immediately, touching nothing.
            var filterId = UUID.randomUUID();
            var roleId = UUID.randomUUID();
            var classField = classFieldOnSchemaSpaceTwinClass(false);
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueUser(classField);
            value.add(new UserEntity().setId(UUID.randomUUID()));
            var collector = new TwinChangesCollector();

            fieldTyper.serializeValue(properties(filterId, roleId, "0"), twin, value, collector);

            verifyNoInteractions(spaceRoleUserService);
            verifyNoInteractions(authService);
            verifyNoInteractions(userService);
        }
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_loadsStoredSpaceRoleUsersByRole() throws ServiceException {
            // Intended: deserialization reads the twin's stored space-role-user entities grouped by role id.
            var filterId = UUID.randomUUID();
            var roleId = UUID.randomUUID();
            var classField = classFieldOnSchemaSpaceTwinClass(true);
            var u1 = new UserEntity().setId(UUID.randomUUID());
            var u2 = new UserEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var stored1 = new SpaceRoleUserEntity().setUser(u1).setUserId(u1.getId()).setSpaceRoleId(roleId);
            var stored2 = new SpaceRoleUserEntity().setUser(u2).setUserId(u2.getId()).setSpaceRoleId(roleId);
            twin.setTwinFieldSpaceUserKit(new KitGrouped<>(List.of(stored1, stored2), SpaceRoleUserEntity::getUserId, SpaceRoleUserEntity::getSpaceRoleId));

            FieldValueUser result = fieldTyper.deserializeValue(properties(filterId, roleId, "0"), twinField(twin, classField));

            // KitGrouped.getGrouped is backed by a HashMap → order is not guaranteed; assert membership.
            assertEquals(2, result.getItems().size());
            assertTrue(result.getItems().contains(u1));
            assertTrue(result.getItems().contains(u2));
        }
    }
}
