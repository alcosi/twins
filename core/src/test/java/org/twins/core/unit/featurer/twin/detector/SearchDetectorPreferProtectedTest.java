package org.twins.core.featurer.twin.detector;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.search.TwinSearchEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.PermissionService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchDetectorPreferProtectedTest extends BaseUnitTest {

    @Mock
    AuthService authService;

    @Mock
    PermissionService permissionService;

    @Mock
    ApiUser apiUser;

    private SearchDetectorPreferProtected detector;

    @BeforeEach
    void setUp() throws Exception {
        detector = new SearchDetectorPreferProtected();
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
        void detect_emptyList_throwsTwinSearchAliasUnknown() throws ServiceException {
            when(authService.getApiUser()).thenReturn(apiUser);

            var ex = assertThrows(ServiceException.class,
                    () -> detector.detect(new java.util.Properties(), new ArrayList<>()));
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_ALIAS_UNKNOWN.getCode(), ex.getErrorCode());
        }

        @Test
        void detect_singleSearchNoPermission_returnsIt() throws ServiceException {
            when(authService.getApiUser()).thenReturn(apiUser);
            var search = searchEntity(UUID.randomUUID(), null);
            var searches = List.of(search);

            var result = detector.detect(new java.util.Properties(), searches);

            assertEquals(1, result.size());
            assertEquals(search, result.get(0));
        }

        @Test
        void detect_singleSearchWithMatchingPermission_returnsIt() throws ServiceException {
            var perm = UUID.randomUUID();
            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getPermissions()).thenReturn(java.util.Set.of(perm));
            var search = searchEntity(UUID.randomUUID(), perm);
            var searches = List.of(search);

            var result = detector.detect(new java.util.Properties(), searches);

            assertEquals(1, result.size());
            assertEquals(search, result.get(0));
        }

        @Test
        void detect_noMatchingPermissions_throwsTwinSearchAliasUnknown() throws ServiceException {
            var perm = UUID.randomUUID();
            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getPermissions()).thenReturn(Collections.emptySet());
            var search = searchEntity(UUID.randomUUID(), perm);
            var searches = List.of(search);

            var ex = assertThrows(ServiceException.class,
                    () -> detector.detect(new java.util.Properties(), searches));
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_ALIAS_UNKNOWN.getCode(), ex.getErrorCode());
        }

        @Test
        void detect_multipleMatchingSearches_throwsTwinSearchNotUniq() throws ServiceException {
            when(authService.getApiUser()).thenReturn(apiUser);
            var search1 = searchEntity(UUID.randomUUID(), null);
            var search2 = searchEntity(UUID.randomUUID(), null);
            var searches = List.of(search1, search2);

            var ex = assertThrows(ServiceException.class,
                    () -> detector.detect(new java.util.Properties(), searches));
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_NOT_UNIQ.getCode(), ex.getErrorCode());
        }

        @Test
        void detect_multipleSearchesOneMatches_returnsSingleMatch() throws ServiceException {
            var permAllowed = UUID.randomUUID();
            var permDenied = UUID.randomUUID();
            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getPermissions()).thenReturn(java.util.Set.of(permAllowed));
            var searchAllowed = searchEntity(UUID.randomUUID(), permAllowed);
            var searchDenied = searchEntity(UUID.randomUUID(), permDenied);
            var searches = List.of(searchAllowed, searchDenied);

            var result = detector.detect(new java.util.Properties(), searches);

            assertEquals(1, result.size());
            assertEquals(searchAllowed, result.get(0));
        }

        @Test
        void detect_mixedNullPermissionAndNoMatch_throwsAliasUnknown() throws ServiceException {
            var perm = UUID.randomUUID();
            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getPermissions()).thenReturn(java.util.Set.of(perm));
            var searchWithPerm = searchEntity(UUID.randomUUID(), UUID.randomUUID());
            var searches = List.of(searchWithPerm);

            var ex = assertThrows(ServiceException.class,
                    () -> detector.detect(new java.util.Properties(), searches));
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_ALIAS_UNKNOWN.getCode(), ex.getErrorCode());
        }
    }
}
