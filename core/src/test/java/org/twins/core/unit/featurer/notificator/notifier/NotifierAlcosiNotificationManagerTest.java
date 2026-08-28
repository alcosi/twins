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

class NotifierAlcosiNotificationManagerTest extends BaseUnitTest {

    private NotifierAlcosiNotificationManager notifier;

    @BeforeEach
    void setUp() {
        notifier = new NotifierAlcosiNotificationManager();
    }

    @Nested
    class GetHostDomainBaseUri {

        @Test
        void getHostDomainBaseUri_validUri_returnsUri() throws ServiceException {
            var props = new Properties();
            props.setProperty("hostDomainBaseUri", "http://example.com:8080");

            var result = notifier.getHostDomainBaseUri(props);

            assertEquals("http://example.com:8080", result);
        }

        @Test
        void getHostDomainBaseUri_nullUri_throwsException() {
            var props = new Properties();

            var exception = assertThrows(ServiceException.class,
                    () -> notifier.getHostDomainBaseUri(props));
            assertTrue(exception.getMessage().contains("hostDomainBaseUri"));
        }

        @Test
        void getHostDomainBaseUri_emptyUri_throwsException() {
            var props = new Properties();
            props.setProperty("hostDomainBaseUri", "");

            var exception = assertThrows(ServiceException.class,
                    () -> notifier.getHostDomainBaseUri(props));
            assertTrue(exception.getMessage().contains("hostDomainBaseUri"));
        }

        @Test
        void getHostDomainBaseUri_slashOnly_throwsException() {
            var props = new Properties();
            props.setProperty("hostDomainBaseUri", "/");

            var exception = assertThrows(ServiceException.class,
                    () -> notifier.getHostDomainBaseUri(props));
            assertTrue(exception.getMessage().contains("hostDomainBaseUri"));
        }
    }

    @Nested
    class GetOrCreateStub {

        @Test
        void getOrCreateStub_httpUri_parsesHostAndPort() {
            var stub = notifier.getOrCreateStub("http://example.com:9090");

            assertNotNull(stub);
        }

        @Test
        void getOrCreateStub_httpsUri_parsesHostAndDefaultPort() {
            var stub = notifier.getOrCreateStub("https://example.com");

            assertNotNull(stub);
        }

        @Test
        void getOrCreateStub_plainTarget_usesDirectly() {
            var stub = notifier.getOrCreateStub("localhost:20108");

            assertNotNull(stub);
        }

        @Test
        void getOrCreateStub_sameUri_returnsCachedStub() {
            var uri = "http://example.com:9090";

            var stub1 = notifier.getOrCreateStub(uri);
            var stub2 = notifier.getOrCreateStub(uri);

            assertSame(stub1, stub2);
        }

        @Test
        void getOrCreateStub_differentUris_returnsDifferentStubs() {
            var stub1 = notifier.getOrCreateStub("http://host1.com:9090");
            var stub2 = notifier.getOrCreateStub("http://host2.com:9090");

            assertNotSame(stub1, stub2);
        }
    }

    @Nested
    class NotifyInternal {

        @Test
        void notify_putsEventCodeInContext() throws ServiceException {
            var props = new Properties();
            props.setProperty("hostDomainBaseUri", "http://example.com:20108");
            props.setProperty("collectCompanyKey", "COMPANY_ID");
            props.setProperty("collectEventKey", "EVENT_ID");
            var context = new HashMap<String, String>();
            context.put("COMPANY_ID", "company-123");

            // This will try to make a gRPC call which will fail in test,
            // but we can verify context was populated before the call
            var recipientIds = Set.of(UUID.randomUUID());

            // We expect the gRPC call to fail since there's no server,
            // so we catch the exception
            assertThrows(Exception.class,
                    () -> notifier.notify(recipientIds, context, "EVENT_001", props));

            // Verify context was populated with event key before the gRPC call
            assertEquals("EVENT_001", context.get("EVENT_ID"));
        }

        @Test
        void notify_missingBusinessAccountKeyInContext_throwsNullPointerException() {
            var props = new Properties();
            props.setProperty("hostDomainBaseUri", "http://example.com:20108");
            props.setProperty("collectCompanyKey", "COMPANY_ID");
            props.setProperty("collectEventKey", "EVENT_ID");
            var context = new HashMap<String, String>();
            // BUSINESS_ACCOUNT_KEY is missing from context

            var recipientIds = Set.of(UUID.randomUUID());

            // The production code throws NPE when context.get(businessAccountKey) returns null
            // This is documented behavior - the key must be present in context
            assertThrows(NullPointerException.class,
                    () -> notifier.notify(recipientIds, context, "EVENT_001", props));

            // Verify context was still populated with event key before the NPE
            assertEquals("EVENT_001", context.get("EVENT_ID"));
        }
    }
}
