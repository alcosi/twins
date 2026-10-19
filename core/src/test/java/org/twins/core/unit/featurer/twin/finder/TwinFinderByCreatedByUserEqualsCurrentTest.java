package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.service.auth.AuthService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFinderByCreatedByUserEqualsCurrentTest extends BaseUnitTest {

    @Mock
    AuthService authService;

    @Mock
    ApiUser apiUser;

    private TwinFinderByCreatedByUserEqualsCurrent finder;

    @BeforeEach
    void setUp() throws Exception {
        finder = new TwinFinderByCreatedByUserEqualsCurrent();
        setField(finder, "authService", authService);
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

    @Nested
    class Concat {

        @Test
        void concat_excludeFalse_addsToCreatedByUserIdList() throws ServiceException {
            var userId = UUID.randomUUID();
            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getUserId()).thenReturn(userId);

            var properties = new Properties();
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getCreatedByUserIdList());
            assertTrue(twinSearch.getCreatedByUserIdList().contains(userId));
        }

        @Test
        void concat_excludeTrue_addsToCreatedByUserIdExcludeList() throws ServiceException {
            var userId = UUID.randomUUID();
            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getUserId()).thenReturn(userId);

            var properties = new Properties();
            properties.setProperty("exclude", "true");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getCreatedByUserIdExcludeList());
            assertTrue(twinSearch.getCreatedByUserIdExcludeList().contains(userId));
        }
    }
}
