package org.twins.core.featurer.twin.detector;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.search.TwinSearchEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.PermissionService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchDetectorConcatTest extends BaseUnitTest {

    @Mock
    AuthService authService;

    @Mock
    PermissionService permissionService;

    @Mock
    ApiUser apiUser;

    private SearchDetectorConcat detector;

    @BeforeEach
    void setUp() throws Exception {
        detector = new SearchDetectorConcat();
        setField(detector, "authService", authService);
        setField(detector, "permissionService", permissionService);
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

    private TwinSearchEntity searchEntity(UUID id, UUID permissionId) {
        var entity = new TwinSearchEntity();
        entity.setId(id);
        entity.setPermissionId(permissionId);
        return entity;
    }

    @Nested
    class Detect {

        @Test
        void detect_emptyList_returnsEmptyList() throws ServiceException {
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = detector.detect(new java.util.Properties(), new ArrayList<>());

            assertTrue(result.isEmpty());
        }

        @Test
        void detect_allSearchesWithoutPermission_returnsAll() throws ServiceException {
            when(authService.getApiUser()).thenReturn(apiUser);
            var search1 = searchEntity(UUID.randomUUID(), null);
            var search2 = searchEntity(UUID.randomUUID(), null);
            var searches = List.of(search1, search2);

            var result = detector.detect(new java.util.Properties(), searches);

            assertEquals(2, result.size());
            assertTrue(result.contains(search1));
            assertTrue(result.contains(search2));
        }

        @Test
        void detect_allSearchesWithMatchingPermissions_returnsAll() throws ServiceException {
            var perm1 = UUID.randomUUID();
            var perm2 = UUID.randomUUID();
            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getPermissions()).thenReturn(java.util.Set.of(perm1, perm2));
            var search1 = searchEntity(UUID.randomUUID(), perm1);
            var search2 = searchEntity(UUID.randomUUID(), perm2);
            var searches = List.of(search1, search2);

            var result = detector.detect(new java.util.Properties(), searches);

            assertEquals(2, result.size());
            assertTrue(result.contains(search1));
            assertTrue(result.contains(search2));
        }

        @Test
        void detect_mixedPermissions_returnsOnlyPermittedAndNullPermission() throws ServiceException {
            var perm1 = UUID.randomUUID();
            var permDenied = UUID.randomUUID();
            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getPermissions()).thenReturn(java.util.Set.of(perm1));
            var searchNoPerm = searchEntity(UUID.randomUUID(), null);
            var searchAllowed = searchEntity(UUID.randomUUID(), perm1);
            var searchDenied = searchEntity(UUID.randomUUID(), permDenied);
            var searches = List.of(searchNoPerm, searchAllowed, searchDenied);

            var result = detector.detect(new java.util.Properties(), searches);

            assertEquals(2, result.size());
            assertTrue(result.contains(searchNoPerm));
            assertTrue(result.contains(searchAllowed));
            assertFalse(result.contains(searchDenied));
        }

        @Test
        void detect_userHasNoPermissions_returnsOnlyNullPermissionSearches() throws ServiceException {
            var perm = UUID.randomUUID();
            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getPermissions()).thenReturn(Collections.EMPTY_SET);
            var searchNoPerm = searchEntity(UUID.randomUUID(), null);
            var searchWithPerm = searchEntity(UUID.randomUUID(), perm);
            var searches = List.of(searchNoPerm, searchWithPerm);

            var result = detector.detect(new java.util.Properties(), searches);

            assertEquals(1, result.size());
            assertTrue(result.contains(searchNoPerm));
        }

        @Test
        void detect_callsLoadCurrentUserPermissions() throws ServiceException {
            when(authService.getApiUser()).thenReturn(apiUser);

            detector.detect(new java.util.Properties(), new ArrayList<>());

            verify(permissionService).loadCurrentUserPermissions();
        }
    }
}
