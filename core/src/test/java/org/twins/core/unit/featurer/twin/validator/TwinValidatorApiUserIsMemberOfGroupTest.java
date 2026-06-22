package org.twins.core.featurer.twin.validator;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.usergroup.UserGroupService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TwinValidatorApiUserIsMemberOfGroupTest extends BaseUnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserGroupService userGroupService;

    @Mock
    private ApiUser apiUser;

    private TwinValidatorApiUserIsMemberOfGroup validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = new TwinValidatorApiUserIsMemberOfGroup();
        setField(validator, "authService", authService);
        setField(validator, "userGroupService", userGroupService);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try { return clazz.getDeclaredField(fieldName); }
            catch (NoSuchFieldException e) { clazz = clazz.getSuperclass(); }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    private UserEntity userWithGroups(Kit<UserGroupEntity, UUID> kit) throws Exception {
        var user = new UserEntity();
        var idField = UserEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, UUID.randomUUID());
        user.setUserGroups(kit);
        return user;
    }

    @Nested
    class IsValid {

        @Test
        void isValid_userIsMemberOfGroup_returnsValid() throws Exception {
            var groupId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            var kit = new Kit<>(UserGroupEntity::getId);
            var group = new UserGroupEntity();
            group.setId(groupId);
            kit.add(group);
            var user = userWithGroups(kit);

            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getUser()).thenReturn(user);
            when(apiUser.getBusinessAccountId()).thenReturn(UUID.randomUUID());

            var props = new Properties();
            props.put("userGroupIds", groupId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
            verify(userGroupService).loadGroupsForCurrentUser();
        }

        @Test
        void isValid_userNotMemberOfGroup_returnsInvalid() throws Exception {
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            var userGroupId = UUID.randomUUID();
            var kit = new Kit<>(UserGroupEntity::getId);
            var group = new UserGroupEntity();
            group.setId(userGroupId);
            kit.add(group);
            var user = userWithGroups(kit);

            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getUser()).thenReturn(user);

            var props = new Properties();
            props.put("userGroupIds", UUID.randomUUID().toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_nullUserGroups_returnsInvalid() throws Exception {
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            var user = new UserEntity();
            var idField = UserEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, UUID.randomUUID());

            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getUser()).thenReturn(user);

            var props = new Properties();
            props.put("userGroupIds", UUID.randomUUID().toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_emptyUserGroups_returnsInvalid() throws Exception {
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            var kit = new Kit<>(UserGroupEntity::getId);
            var user = userWithGroups(kit);

            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getUser()).thenReturn(user);

            var props = new Properties();
            props.put("userGroupIds", UUID.randomUUID().toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_userIsMemberOfGroup_inverted_returnsInvalid() throws Exception {
            var groupId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            var kit = new Kit<>(UserGroupEntity::getId);
            var group = new UserGroupEntity();
            group.setId(groupId);
            kit.add(group);
            var user = userWithGroups(kit);

            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getUser()).thenReturn(user);
            when(apiUser.getBusinessAccountId()).thenReturn(UUID.randomUUID());

            var props = new Properties();
            props.put("userGroupIds", groupId.toString());

            var result = validator.isValid(props, List.of(twin), true);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_userNotMemberOfGroup_inverted_returnsValid() throws Exception {
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            var userGroupId = UUID.randomUUID();
            var kit = new Kit<>(UserGroupEntity::getId);
            var group = new UserGroupEntity();
            group.setId(userGroupId);
            kit.add(group);
            var user = userWithGroups(kit);

            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getUser()).thenReturn(user);

            var props = new Properties();
            props.put("userGroupIds", UUID.randomUUID().toString());

            var result = validator.isValid(props, List.of(twin), true);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
        }
    }
}
