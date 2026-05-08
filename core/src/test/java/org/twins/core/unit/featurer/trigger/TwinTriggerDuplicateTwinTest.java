package org.twins.core.featurer.trigger;

import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinTriggerDuplicateTwinTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private AuthService authService;

    private TwinTriggerDuplicateTwin trigger;

    @BeforeEach
    void setUp() throws Exception {
        trigger = new TwinTriggerDuplicateTwin(twinService, authService);
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

    private TwinEntity buildTwin() {
        var twin = new TwinEntity();
        twin.setId(UUID.randomUUID());
        return twin;
    }

    private Properties buildProperties(UUID twinIdValue) {
        var props = new Properties();
        props.setProperty("twinId", twinIdValue.toString());
        return props;
    }

    @Nested
    class Run {

        @Test
        void run_withFoundTwin_duplicatesTwin() throws ServiceException {
            var twin = buildTwin();
            var srcTwinId = UUID.randomUUID();
            var props = buildProperties(srcTwinId);
            var srcTwin = new TwinEntity();
            srcTwin.setId(srcTwinId);

            when(twinService.findEntity(srcTwinId, EntitySmartService.FindMode.ifEmptyNull, EntitySmartService.ReadPermissionCheckMode.ifDeniedLog))
                    .thenReturn(srcTwin);

            trigger.run(props, twin, null, null, null);

            verify(twinService).duplicateTwin(srcTwin, null);
        }

        @Test
        void run_withNullTwin_doesNotDuplicate() throws ServiceException {
            var twin = buildTwin();
            var srcTwinId = UUID.randomUUID();
            var props = buildProperties(srcTwinId);

            when(twinService.findEntity(srcTwinId, EntitySmartService.FindMode.ifEmptyNull, EntitySmartService.ReadPermissionCheckMode.ifDeniedLog))
                    .thenReturn(null);

            trigger.run(props, twin, null, null, null);

            verify(twinService, never()).duplicateTwin(any(TwinEntity.class), any());
        }
    }
}
