package org.twins.core.featurer.twin.validator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.service.twin.TwinSearchServiceV2;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TwinValidatorTwinAllChildrenInStatusesTest extends BaseUnitTest {

    @Mock
    private TwinSearchServiceV2 twinSearchService;

    private TwinValidatorTwinAllChildrenInStatuses validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = new TwinValidatorTwinAllChildrenInStatuses();
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
        void isValid_noChildrenInStatus_returnsValid() throws ServiceException {
            var classId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            when(twinSearchService.countByGroupFields(any(BasicSearch.class), eq(TwinEntity.BasicField.HEAD_TWIN_ID)))
                    .thenReturn(Collections.emptyMap());

            var props = new Properties();
            props.put("childrenTwinClassId", classId.toString());
            props.put("childrenTwinStatusId", statusId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twinId).isValid());
        }

        @Test
        void isValid_zeroChildrenInStatus_returnsValid() throws ServiceException {
            var classId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            when(twinSearchService.countByGroupFields(any(BasicSearch.class), eq(TwinEntity.BasicField.HEAD_TWIN_ID)))
                    .thenReturn(Map.of(twinId, 0L));

            var props = new Properties();
            props.put("childrenTwinClassId", classId.toString());
            props.put("childrenTwinStatusId", statusId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twinId).isValid());
        }

        @Test
        void isValid_hasChildrenInStatus_returnsInvalid() throws ServiceException {
            var classId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            when(twinSearchService.countByGroupFields(any(BasicSearch.class), eq(TwinEntity.BasicField.HEAD_TWIN_ID)))
                    .thenReturn(Map.of(twinId, 3L));

            var props = new Properties();
            props.put("childrenTwinClassId", classId.toString());
            props.put("childrenTwinStatusId", statusId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twinId).isValid());
        }

        @Test
        void isValid_hasChildrenInStatus_inverted_returnsValid() throws ServiceException {
            var classId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            when(twinSearchService.countByGroupFields(any(BasicSearch.class), eq(TwinEntity.BasicField.HEAD_TWIN_ID)))
                    .thenReturn(Map.of(twinId, 3L));

            var props = new Properties();
            props.put("childrenTwinClassId", classId.toString());
            props.put("childrenTwinStatusId", statusId.toString());

            var result = validator.isValid(props, List.of(twin), true);

            assertTrue(result.getTwinsResults().get(twinId).isValid());
        }
    }
}
