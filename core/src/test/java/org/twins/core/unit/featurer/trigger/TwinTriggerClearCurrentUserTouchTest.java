package org.twins.core.featurer.trigger;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.enums.twin.Touch;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinTouchService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinTriggerClearCurrentUserTouchTest extends BaseUnitTest {

    @Mock
    private TwinTouchService twinTouchService;

    @Mock
    private AuthService authService;

    private TwinTriggerClearCurrentUserTouch trigger;

    @BeforeEach
    void setUp() throws Exception {
        trigger = new TwinTriggerClearCurrentUserTouch(twinTouchService, authService);
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

    private Properties buildProperties(String touchIdValue) {
        var props = new Properties();
        props.setProperty("touchId", touchIdValue);
        return props;
    }

    @Nested
    class Run {

        @Test
        void run_delegatesToTouchService() throws ServiceException {
            var twin = buildTwin();
            var props = buildProperties("WATCHED");

            trigger.run(props, twin, null, null, null);

            verify(twinTouchService).deleteCurrentUserTouch(twin.getId(), Touch.WATCHED);
        }

        @Test
        void run_callsDeleteCurrentUserTouch_notDeleteAllUsersTouch() throws ServiceException {
            var twin = buildTwin();
            var props = buildProperties("WATCHED");

            trigger.run(props, twin, null, null, null);

            // Verify deleteCurrentUserTouch is called
            verify(twinTouchService).deleteCurrentUserTouch(twin.getId(), Touch.WATCHED);
            // Verify deleteAllUsersTouch is NOT called (ensuring we don't accidentally clear all users)
            verify(twinTouchService, never()).deleteAllUsersTouch(any(), any());
        }
    }
}
