package org.twins.core.featurer.trigger.messaging.rabbitmq;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.trigger.messaging.rabbitmq.payloads.RabbitMqMessagePayloadTwin;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.rabbit.AmpqManager;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinTriggerRabbitMqSendTwinTest extends BaseUnitTest {

    @Mock
    private AmpqManager ampqManager;

    @Mock
    private AuthService authService;

    private TwinTriggerRabbitMqSendTwin trigger;

    @BeforeEach
    void setUp() throws Exception {
        trigger = new TwinTriggerRabbitMqSendTwin(ampqManager, authService);
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

    private Properties buildProperties(String url, String exchange, String queue, String operation) {
        var props = new Properties();
        props.setProperty("url", url);
        props.setProperty("exchange", exchange);
        props.setProperty("queue", queue);
        props.setProperty("operation", operation);
        return props;
    }

    @Nested
    class Connect {

        @Test
        void connect_createsNewConnectionFactoryForNewUrl() {
            var props = buildProperties("amqp://localhost:5672", "ex", "q", "op");

            trigger.connect(props);

            var cachedFactory = TwinTriggerRabbitMqConnection.rabbitConnectionCache.get("amqp://localhost:5672");
            assertNotNull(cachedFactory);
        }

        @Test
        void connect_withSameUrl_reusesCachedConnectionFactory() {
            var props1 = buildProperties("amqp://localhost:5679", "ex", "q", "op");
            var props2 = buildProperties("amqp://localhost:5679", "ex", "q", "op");

            trigger.connect(props1);
            var firstFactory = TwinTriggerRabbitMqConnection.rabbitConnectionCache.get("amqp://localhost:5679");

            trigger.connect(props2);
            var secondFactory = TwinTriggerRabbitMqConnection.rabbitConnectionCache.get("amqp://localhost:5679");

            assertNotNull(firstFactory);
            assertSame(firstFactory, secondFactory, "Should reuse the same cached factory for same URL");
        }
    }

    @Nested
    class Send {

        @Test
        void send_sendsPayloadWithCorrectParameters() throws ServiceException {
            var twin = buildTwin();
            var userId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            var businessAccountId = UUID.randomUUID();
            var jobTwinId = UUID.randomUUID();
            var props = buildProperties("amqp://localhost:5673", "testExchange", "testQueue", "CREATE");

            var apiUser = mock(ApiUser.class);
            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getUserId()).thenReturn(userId);
            when(apiUser.getDomainId()).thenReturn(domainId);
            when(apiUser.getBusinessAccountId()).thenReturn(businessAccountId);

            // Pre-cache a mock ConnectionFactory
            var mockFactory = mock(CachingConnectionFactory.class);
            TwinTriggerRabbitMqConnection.rabbitConnectionCache.put("amqp://localhost:5673", mockFactory);

            trigger.send(props, twin, null, null, jobTwinId);

            var captor = ArgumentCaptor.forClass(RabbitMqMessagePayloadTwin.class);
            verify(ampqManager).sendMessage(eq(mockFactory), eq("testExchange"), eq("testQueue"), captor.capture());

            var payload = captor.getValue();
            assertEquals(twin.getId(), payload.twinsId());
            assertEquals(userId, payload.userId());
            assertEquals(domainId, payload.domainId());
            assertEquals(businessAccountId, payload.businessAccountId());
            assertEquals("CREATE", payload.operation());
            assertEquals(jobTwinId, payload.jobTwinId());
        }
    }
}
