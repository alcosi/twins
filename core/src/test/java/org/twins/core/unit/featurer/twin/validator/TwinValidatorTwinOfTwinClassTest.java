package org.twins.core.featurer.twin.validator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.service.twin.TwinSearchService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TwinValidatorTwinOfTwinClassTest extends BaseUnitTest {

    @Mock
    private TwinSearchService twinSearchService;

    private TwinValidatorTwinOfTwinClass validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = new TwinValidatorTwinOfTwinClass();
        setField(validator, "twinSearchService", twinSearchService);
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

    @Nested
    class IsValid {

        @Test
        void isValid_twinsExistOfClass_returnsValid() throws ServiceException {
            var classId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            when(twinSearchService.count(any(BasicSearch.class))).thenReturn(5L);

            var props = new Properties();
            props.put("classIds", classId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_noTwinsOfClass_returnsInvalid() throws ServiceException {
            var classId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            when(twinSearchService.count(any(BasicSearch.class))).thenReturn(0L);

            var props = new Properties();
            props.put("classIds", classId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_twinsExistOfClass_inverted_returnsInvalid() throws ServiceException {
            var classId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            when(twinSearchService.count(any(BasicSearch.class))).thenReturn(5L);

            var props = new Properties();
            props.put("classIds", classId.toString());

            var result = validator.isValid(props, List.of(twin), true);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_noTwinsOfClass_inverted_returnsValid() throws ServiceException {
            var classId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            when(twinSearchService.count(any(BasicSearch.class))).thenReturn(0L);

            var props = new Properties();
            props.put("classIds", classId.toString());

            var result = validator.isValid(props, List.of(twin), true);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
        }
    }
}
