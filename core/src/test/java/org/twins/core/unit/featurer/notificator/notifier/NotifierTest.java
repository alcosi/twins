package org.twins.core.featurer.notificator.notifier;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotifierTest extends BaseUnitTest {

    private TestableNotifier notifier;

    @BeforeEach
    void setUp() {
        notifier = new TestableNotifier();
    }

    /**
     * Concrete test subclass that captures per-event notify calls (the {@link NotifierAtomic} hook).
     */
    static class TestableNotifier extends NotifierAtomic {

        Set<UUID> lastRecipientIds;
        Map<String, String> lastContext;
        String lastEventCode;
        Properties lastProperties;
        int notifyCallCount;

        @Override
        protected void notify(Set<UUID> recipientIds, Map<String, String> context,
                              String eventCode, Properties properties) {
            lastRecipientIds = recipientIds;
            lastContext = context;
            lastEventCode = eventCode;
            lastProperties = properties;
            notifyCallCount++;
        }
    }

    @Nested
    class ValidateContext {

        @Test
        void validateContext_noNullValues_doesNotThrow() throws ServiceException {
            var context = new HashMap<String, String>();
            context.put("key1", "value1");
            context.put("key2", "value2");

            notifier.validateContext(context, true);

            assertEquals(2, context.size());
        }

        @Test
        void validateContext_withNullValueAndThrowTrue_throwsException() {
            var context = new HashMap<String, String>();
            context.put("key1", "value1");
            context.put("key2", null);

            var exception = assertThrows(ServiceException.class,
                    () -> notifier.validateContext(context, true));
            assertTrue(exception.getMessage().contains("key2"));
        }

        @Test
        void validateContext_withNullValueAndThrowFalse_removesEntry() throws ServiceException {
            var context = new HashMap<String, String>();
            context.put("key1", "value1");
            context.put("key2", null);

            notifier.validateContext(context, false);

            assertEquals(1, context.size());
            assertTrue(context.containsKey("key1"));
            assertFalse(context.containsKey("key2"));
        }

        @Test
        void validateContext_allNullValuesAndThrowFalse_removesAll() throws ServiceException {
            var context = new HashMap<String, String>();
            context.put("key1", null);
            context.put("key2", null);

            notifier.validateContext(context, false);

            assertTrue(context.isEmpty());
        }
    }

    @Nested
    class NotifyPublicMethod {

        @Test
        void notify_validContext_delegatesPerEventAndReturnsNoFailures() throws Exception {
            var recipientIds = Set.of(UUID.randomUUID());
            var context = new HashMap<String, String>();
            context.put("key1", "value1");
            var notifierParams = new HashMap<String, String>();
            notifierParams.put("throwExceptionOnNullValues", "true");
            var props = new Properties();
            props.setProperty("throwExceptionOnNullValues", "true");
            var featurerService = mock(org.cambium.featurer.FeaturerService.class);
            when(featurerService.extractProperties(any(org.cambium.featurer.Featurer.class), eq(notifierParams))).thenReturn(props);
            setField(notifier, "featurerService", featurerService);

            var failed = notifier.notify(notifierParams, new java.util.LinkedHashSet<>(java.util.List.of(
                    new NotifyEvent(null, recipientIds, context, "eventCode"))));

            assertTrue(failed.isEmpty());
            assertEquals(1, notifier.notifyCallCount);
            assertEquals(recipientIds, notifier.lastRecipientIds);
            assertEquals("eventCode", notifier.lastEventCode);
        }

        @Test
        void notify_nullContextValue_throwsBeforeSending() throws Exception {
            var notifierParams = new HashMap<String, String>();
            notifierParams.put("throwExceptionOnNullValues", "true");
            var props = new Properties();
            props.setProperty("throwExceptionOnNullValues", "true");
            var featurerService = mock(org.cambium.featurer.FeaturerService.class);
            when(featurerService.extractProperties(any(org.cambium.featurer.Featurer.class), eq(notifierParams))).thenReturn(props);
            setField(notifier, "featurerService", featurerService);
            var context = new HashMap<String, String>();
            context.put("key1", null);

            var exception = assertThrows(ServiceException.class, () -> notifier.notify(notifierParams,
                    new java.util.LinkedHashSet<>(java.util.List.of(new NotifyEvent(null, Set.of(UUID.randomUUID()), context, "eventCode")))));

            assertTrue(exception.getMessage().contains("key1"));
            assertEquals(0, notifier.notifyCallCount);
        }

        private void setField(Object target, String fieldName, Object value) throws Exception {
            Class<?> clazz = target.getClass();
            while (clazz != null) {
                try {
                    var field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(target, value);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            throw new NoSuchFieldException(fieldName);
        }
    }
}
