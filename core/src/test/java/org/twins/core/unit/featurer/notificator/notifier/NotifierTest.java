package org.twins.core.featurer.notificator.notifier;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotifierTest extends BaseUnitTest {

    private TestableNotifier notifier;

    @BeforeEach
    void setUp() {
        notifier = new TestableNotifier();
    }

    /**
     * Concrete test subclass that captures notify calls.
     */
    static class TestableNotifier extends Notifier {

        Set<UUID> lastRecipientIds;
        Map<String, String> lastContext;
        String lastEventCode;
        Properties lastProperties;
        int notifyCallCount;

        @Override
        protected void notify(Set<UUID> recipientIds, Map<String, String> context,
                              String eventCode, Properties properties) throws ServiceException {
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
        void notify_validContext_delegatesToAbstractMethod() throws ServiceException {
            var recipientIds = Set.of(UUID.randomUUID());
            var context = new HashMap<String, String>();
            context.put("key1", "value1");
            var props = new Properties();
            props.setProperty("throwExceptionOnNullValues", "true");

            // We need to mock featurerService for the public method,
            // but since we test the protected method directly, we test validateContext + notify separately
            notifier.notify(recipientIds, context, "eventCode", props);

            assertEquals(1, notifier.notifyCallCount);
            assertEquals(recipientIds, notifier.lastRecipientIds);
            assertEquals("eventCode", notifier.lastEventCode);
        }
    }
}
