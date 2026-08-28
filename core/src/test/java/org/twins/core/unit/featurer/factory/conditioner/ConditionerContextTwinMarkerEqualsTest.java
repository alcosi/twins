package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.conditioner.ConditionerContextTwinMarkerEquals;
import org.twins.core.service.twin.TwinMarkerService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerContextTwinMarkerEqualsTest extends BaseUnitTest {

    @Mock
    private TwinMarkerService twinMarkerService;

    private ConditionerContextTwinMarkerEquals conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerContextTwinMarkerEquals();
        setField(conditioner, "twinMarkerService", twinMarkerService);
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

    private Properties buildProperties(UUID markerId) {
        var props = new Properties();
        props.put("markerId", markerId.toString());
        return props;
    }

    @Nested
    class Check {

        @Test
        void check_twinHasMarker_returnsTrue() throws ServiceException {
            var markerId = UUID.randomUUID();
            var twin = mock(TwinEntity.class);
            var item = mock(FactoryItem.class);
            when(item.checkNotMultiplyContextTwin()).thenReturn(twin);
            when(twinMarkerService.hasMarker(eq(twin), eq(markerId))).thenReturn(true);

            assertTrue(conditioner.check(buildProperties(markerId), item));
        }

        @Test
        void check_twinDoesNotHaveMarker_returnsFalse() throws ServiceException {
            var markerId = UUID.randomUUID();
            var twin = mock(TwinEntity.class);
            var item = mock(FactoryItem.class);
            when(item.checkNotMultiplyContextTwin()).thenReturn(twin);
            when(twinMarkerService.hasMarker(eq(twin), eq(markerId))).thenReturn(false);

            assertFalse(conditioner.check(buildProperties(markerId), item));
        }

        @Test
        void check_noSingleContextTwin_returnsFalse() throws ServiceException {
            // contract: when there is no single (non-multiple) context twin, the condition is false
            var markerId = UUID.randomUUID();
            var item = mock(FactoryItem.class);
            when(item.checkNotMultiplyContextTwin()).thenReturn(null);

            assertFalse(conditioner.check(buildProperties(markerId), item));
        }
    }
}
