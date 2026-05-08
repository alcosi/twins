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
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TwinValidatorTwinHasLinkAndDstTwinHasStatusTest extends BaseUnitTest {

    @Mock
    private TwinLinkService twinLinkService;

    private TwinValidatorTwinHasLinkAndDstTwinHasStatus validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = new TwinValidatorTwinHasLinkAndDstTwinHasStatus();
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
        void isValid_hasLinkAndDstStatusInSet_returnsValid() throws ServiceException {
            var linkId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            when(twinLinkService.hasLink(twin, linkId)).thenReturn(true);
            when(twinLinkService.isLinkDstTwinStatusIn(twin, linkId, Set.of(statusId))).thenReturn(true);

            var props = new Properties();
            props.put("linkId", linkId.toString());
            props.put("twinStatusIds", statusId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
            verify(twinLinkService).loadTwinLinks(any(Collection.class));
        }

        @Test
        void isValid_hasLinkButDstStatusNotInSet_returnsInvalid() throws ServiceException {
            var linkId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            when(twinLinkService.hasLink(twin, linkId)).thenReturn(true);
            when(twinLinkService.isLinkDstTwinStatusIn(twin, linkId, Set.of(statusId))).thenReturn(false);

            var props = new Properties();
            props.put("linkId", linkId.toString());
            props.put("twinStatusIds", statusId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_noLink_returnsInvalid() throws ServiceException {
            var linkId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            when(twinLinkService.hasLink(twin, linkId)).thenReturn(false);

            var props = new Properties();
            props.put("linkId", linkId.toString());
            props.put("twinStatusIds", statusId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
            verify(twinLinkService, never()).isLinkDstTwinStatusIn(any(), any(), any());
        }

        @Test
        void isValid_hasLinkAndDstStatusInSet_inverted_returnsInvalid() throws ServiceException {
            var linkId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            when(twinLinkService.hasLink(twin, linkId)).thenReturn(true);
            when(twinLinkService.isLinkDstTwinStatusIn(twin, linkId, Set.of(statusId))).thenReturn(true);

            var props = new Properties();
            props.put("linkId", linkId.toString());
            props.put("twinStatusIds", statusId.toString());

            var result = validator.isValid(props, List.of(twin), true);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }
    }
}
