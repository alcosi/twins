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
import org.twins.core.featurer.trigger.messaging.rabbitmq.payloads.RabbitMqMessagePayloadFields;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.rabbit.AmpqManager;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinTriggerRabbitMqSendFieldsTest extends BaseUnitTest {

    @Mock
    private AmpqManager ampqManager;

    @Mock
    private AuthService authService;

    private TwinTriggerRabbitMqSendFields trigger;

    @BeforeEach
    void setUp() throws Exception {
        trigger = new TwinTriggerRabbitMqSendFields(ampqManager, authService);
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

    private Properties buildProperties(String url, String exchange, String queue, String operation,
                                       Set<UUID> fields, Set<UUID> excludeInfoFields) {
        var props = new Properties();
        props.setProperty("url", url);
        props.setProperty("exchange", exchange);
        props.setProperty("queue", queue);
        props.setProperty("operation", operation);
        props.setProperty("fields", String.join(",", fields.stream().map(UUID::toString).toList()));
        props.setProperty("excludeInfoFields", String.join(",", excludeInfoFields.stream().map(UUID::toString).toList()));
        return props;
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
            var field1 = UUID.randomUUID();
            var field2 = UUID.randomUUID();
            var excludeField1 = UUID.randomUUID();
            var fields = Set.of(field1, field2);
            var excludeInfoFields = Set.of(excludeField1);

            var props = buildProperties("amqp://localhost:5674", "ex", "q", "UPDATE", fields, excludeInfoFields);

            var apiUser = mock(ApiUser.class);
            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getUserId()).thenReturn(userId);
            when(apiUser.getDomainId()).thenReturn(domainId);
            when(apiUser.getBusinessAccountId()).thenReturn(businessAccountId);

            var mockFactory = mock(CachingConnectionFactory.class);
            TwinTriggerRabbitMqConnection.rabbitConnectionCache.put("amqp://localhost:5674", mockFactory);

            trigger.send(props, twin, null, null, jobTwinId);

            var captor = ArgumentCaptor.forClass(RabbitMqMessagePayloadFields.class);
            verify(ampqManager).sendMessage(eq(mockFactory), eq("ex"), eq("q"), captor.capture());

            var payload = captor.getValue();
            assertEquals(twin.getId(), payload.twinsId());
            assertEquals(userId, payload.userId());
            assertEquals(businessAccountId, payload.businessAccountId());
            assertEquals(domainId, payload.domainId());
            assertEquals("UPDATE", payload.operation());
            assertEquals(jobTwinId, payload.jobTwinId());
            assertEquals(fields, payload.fields());
            assertEquals(excludeInfoFields, payload.excludeInfoFields());
        }
    }
}
