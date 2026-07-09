package org.twins.core.featurer.storager.local;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.service.auth.AuthService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class StoragerLocalDynamicTest extends BaseUnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private FeaturerService featurerService;

    private StoragerLocalDynamic storager;

    @BeforeEach
    void setUp() throws Exception {
        storager = new StoragerLocalDynamic();
        setField(storager, "authService", authService);
        setField(storager, "featurerService", featurerService);
        setField(storager, "contextPath", "");
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

    private HashMap<String, String> buildParams() {
        var params = new HashMap<String, String>();
        params.put("relativeFileUri", "/public/static-resource/{id}/v1");
        params.put("selfHostDomainBaseUri", "/");
        return params;
    }

    private void stubProperties(HashMap<String, String> params) throws ServiceException {
        var props = new Properties();
        params.forEach(props::setProperty);
        when(featurerService.extractProperties(storager, params)).thenReturn(props);
    }

    @Nested
    class GetFileControllerUri {

        @Test
        void getFileControllerUri_withRelativeFileUri_returnsCorrectUri() throws ServiceException {
            var params = buildParams();
            stubProperties(params);

            var result = storager.getFileControllerUri(params);

            assertEquals("//public/static-resource/{id}/v1", result);
        }

        @Test
        void getFileControllerUri_withDomainBaseUri_prependsDomain() throws ServiceException {
            var params = buildParams();
            params.put("selfHostDomainBaseUri", "https://example.com");
            stubProperties(params);

            var result = storager.getFileControllerUri(params);

            assertEquals("https://example.com//public/static-resource/{id}/v1", result);
        }

        @Test
        void getFileControllerUri_withCustomRelativePath_usesCustomPath() throws ServiceException {
            var params = buildParams();
            params.put("relativeFileUri", "/public/resource/{id}/v2");
            stubProperties(params);

            var result = storager.getFileControllerUri(params);

            assertEquals("//public/resource/{id}/v2", result);
        }
    }
}
