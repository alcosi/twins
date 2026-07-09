package org.twins.core.featurer.twin.validator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.link.TwinLinkService;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TwinValidatorTwinHasBackwardLinkTest extends BaseUnitTest {

    @Mock
    private TwinLinkService twinLinkService;

    private TwinValidatorTwinHasBackwardLink validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = new TwinValidatorTwinHasBackwardLink();
        setField(validator, "twinLinkService", twinLinkService);
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
        void isValid_hasBackwardLink_returnsValid() throws ServiceException {
            var linkId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            when(twinLinkService.countBackwardLinks(List.of(twinId), linkId))
                    .thenReturn(Map.of(twinId, 3));

            var props = new Properties();
            props.put("linkId", linkId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twinId).isValid());
        }

        @Test
        void isValid_noBackwardLink_returnsInvalid() throws ServiceException {
            var linkId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            when(twinLinkService.countBackwardLinks(List.of(twinId), linkId))
                    .thenReturn(Collections.emptyMap());

            var props = new Properties();
            props.put("linkId", linkId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twinId).isValid());
        }

        @Test
        void isValid_zeroCount_returnsInvalid() throws ServiceException {
            var linkId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            when(twinLinkService.countBackwardLinks(List.of(twinId), linkId))
                    .thenReturn(Map.of(twinId, 0));

            var props = new Properties();
            props.put("linkId", linkId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twinId).isValid());
        }

        @Test
        void isValid_hasBackwardLink_inverted_returnsInvalid() throws ServiceException {
            var linkId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            when(twinLinkService.countBackwardLinks(List.of(twinId), linkId))
                    .thenReturn(Map.of(twinId, 1));

            var props = new Properties();
            props.put("linkId", linkId.toString());

            var result = validator.isValid(props, List.of(twin), true);

            assertFalse(result.getTwinsResults().get(twinId).isValid());
        }
    }
}
