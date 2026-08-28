package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.factory.conditioner.ConditionerApiUserIsMemberOfGroup;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.usergroup.UserGroupService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConditionerApiUserIsMemberOfGroupTest extends BaseUnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserGroupService userGroupService;

    @Mock
    private ApiUser apiUser;

    private ConditionerApiUserIsMemberOfGroup conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerApiUserIsMemberOfGroup();
        setField(conditioner, "authService", authService);
        setField(conditioner, "userGroupService", userGroupService);
        when(authService.getApiUser()).thenReturn(apiUser);
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

    private UserEntity userWithGroups(Kit<UserGroupEntity, UUID> kit) throws Exception {
        var user = new UserEntity();
        var idField = UserEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, UUID.randomUUID());
        user.setUserGroups(kit);
        return user;
    }

    private Properties props(UUID groupId) {
        var p = new Properties();
        p.put("userGroupIds", groupId.toString());
        return p;
    }

    @Nested
    class Check {

        @Test
        void check_apiUserMemberOfGroup_returnsTrue() throws Exception {
            var groupId = UUID.randomUUID();
            var kit = new Kit<>(UserGroupEntity::getId);
            var group = new UserGroupEntity();
            group.setId(groupId);
            kit.add(group);
            var user = userWithGroups(kit);
            when(apiUser.getUser()).thenReturn(user);

            assertTrue(conditioner.check(props(groupId), null));
            verify(userGroupService).loadGroupsForCurrentUser();
        }

        @Test
        void check_apiUserNotMemberOfGroup_returnsFalse() throws Exception {
            var userGroupId = UUID.randomUUID();
            var kit = new Kit<>(UserGroupEntity::getId);
            var group = new UserGroupEntity();
            group.setId(userGroupId);
            kit.add(group);
            var user = userWithGroups(kit);
            when(apiUser.getUser()).thenReturn(user);

            assertFalse(conditioner.check(props(UUID.randomUUID()), null));
        }

        @Test
        void check_nullUserGroups_returnsFalse() throws Exception {
            // contract: empty/null groups kit → not member
            var user = new UserEntity();
            var idField = UserEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, UUID.randomUUID());
            when(apiUser.getUser()).thenReturn(user);

            assertFalse(conditioner.check(props(UUID.randomUUID()), null));
        }

        @Test
        void check_multipleParamGroups_apiUserMemberOfAny_returnsTrue() throws Exception {
            // contract: param is a SET of group ids; any-match is enough
            var g1 = UUID.randomUUID();
            var g2 = UUID.randomUUID();
            var kit = new Kit<>(UserGroupEntity::getId);
            var group = new UserGroupEntity();
            group.setId(g2);
            kit.add(group);
            var user = userWithGroups(kit);
            when(apiUser.getUser()).thenReturn(user);

            var p = new Properties();
            p.put("userGroupIds", g1.toString() + "," + g2.toString());

            assertTrue(conditioner.check(p, null));
        }
    }
}
