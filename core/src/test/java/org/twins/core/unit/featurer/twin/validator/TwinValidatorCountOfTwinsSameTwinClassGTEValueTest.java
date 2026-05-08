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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TwinValidatorCountOfTwinsSameTwinClassGTEValueTest extends BaseUnitTest {

    @Mock
    private TwinSearchService twinSearchService;

    private TwinValidatorCountOfTwinsSameTwinClassGTEValue validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = new TwinValidatorCountOfTwinsSameTwinClassGTEValue();
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
        void isValid_countMeetsThreshold_returnsValid() throws ServiceException {
            var classId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setTwinClassId(classId);

            when(twinSearchService.countGroupBy(any(BasicSearch.class), eq(TwinEntity.Fields.twinClassId)))
                    .thenReturn(Map.of(classId, 5L));

            var props = new Properties();
            props.put("GTEvalue", "3");

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_countBelowThreshold_returnsInvalid() throws ServiceException {
            var classId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setTwinClassId(classId);

            when(twinSearchService.countGroupBy(any(BasicSearch.class), eq(TwinEntity.Fields.twinClassId)))
                    .thenReturn(Map.of(classId, 2L));

            var props = new Properties();
            props.put("GTEvalue", "5");

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_countExactlyAtThreshold_returnsValid() throws ServiceException {
            var classId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setTwinClassId(classId);

            when(twinSearchService.countGroupBy(any(BasicSearch.class), eq(TwinEntity.Fields.twinClassId)))
                    .thenReturn(Map.of(classId, 5L));

            var props = new Properties();
            props.put("GTEvalue", "5");

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_noCountForClass_returnsInvalid() throws ServiceException {
            var classId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setTwinClassId(classId);

            when(twinSearchService.countGroupBy(any(BasicSearch.class), eq(TwinEntity.Fields.twinClassId)))
                    .thenReturn(Collections.emptyMap());

            var props = new Properties();
            props.put("GTEvalue", "1");

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_countMeetsThreshold_inverted_returnsInvalid() throws ServiceException {
            var classId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setTwinClassId(classId);

            when(twinSearchService.countGroupBy(any(BasicSearch.class), eq(TwinEntity.Fields.twinClassId)))
                    .thenReturn(Map.of(classId, 5L));

            var props = new Properties();
            props.put("GTEvalue", "3");

            var result = validator.isValid(props, List.of(twin), true);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }
    }
}
