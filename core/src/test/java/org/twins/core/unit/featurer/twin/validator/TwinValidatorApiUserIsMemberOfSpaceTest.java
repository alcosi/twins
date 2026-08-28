package org.twins.core.featurer.twin.validator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TwinValidatorApiUserIsMemberOfSpaceTest extends BaseUnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private SpaceRoleUserRepository spaceRoleUserRepository;

    @Mock
    private ApiUser apiUser;

    private TwinValidatorApiUserIsMemberOfSpace validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = new TwinValidatorApiUserIsMemberOfSpace();
        setField(validator, "authService", authService);
        setField(validator, "spaceRoleUserRepository", spaceRoleUserRepository);
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

    private UserEntity userWithId(UUID userId) throws Exception {
        var user = new UserEntity();
        var idField = UserEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, userId);
        return user;
    }

    @Nested
    class IsValid {

        @Test
        void isValid_userIsMemberOfSpace_returnsValid() throws Exception {
            var spaceRoleId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            var spaceRoleUser = new SpaceRoleUserEntity();
            spaceRoleUser.setId(UUID.randomUUID());
            spaceRoleUser.setTwinId(twinId);

            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getUserId()).thenReturn(userId);
            when(apiUser.getUser()).thenReturn(userWithId(userId));

            when(spaceRoleUserRepository.findAllByTwinIdInAndSpaceRoleIdAndUserId(
                    List.of(twinId), spaceRoleId, userId))
                    .thenReturn(List.of(spaceRoleUser));

            var props = new Properties();
            props.put("spaceRoleId", spaceRoleId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twinId).isValid());
        }

        @Test
        void isValid_userNotMemberOfSpace_returnsInvalid() throws Exception {
            var spaceRoleId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getUserId()).thenReturn(userId);
            when(apiUser.getUser()).thenReturn(userWithId(userId));

            when(spaceRoleUserRepository.findAllByTwinIdInAndSpaceRoleIdAndUserId(
                    List.of(twinId), spaceRoleId, userId))
                    .thenReturn(Collections.emptyList());

            var props = new Properties();
            props.put("spaceRoleId", spaceRoleId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twinId).isValid());
        }

        @Test
        void isValid_userIsMemberOfSpace_inverted_returnsInvalid() throws Exception {
            var spaceRoleId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            var spaceRoleUser = new SpaceRoleUserEntity();
            spaceRoleUser.setId(UUID.randomUUID());
            spaceRoleUser.setTwinId(twinId);

            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getUserId()).thenReturn(userId);
            when(apiUser.getUser()).thenReturn(userWithId(userId));

            when(spaceRoleUserRepository.findAllByTwinIdInAndSpaceRoleIdAndUserId(
                    List.of(twinId), spaceRoleId, userId))
                    .thenReturn(List.of(spaceRoleUser));

            var props = new Properties();
            props.put("spaceRoleId", spaceRoleId.toString());

            var result = validator.isValid(props, List.of(twin), true);

            assertFalse(result.getTwinsResults().get(twinId).isValid());
        }
    }
}
