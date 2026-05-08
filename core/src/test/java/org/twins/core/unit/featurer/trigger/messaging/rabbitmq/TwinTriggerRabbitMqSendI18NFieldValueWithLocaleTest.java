package org.twins.core.featurer.trigger.messaging.rabbitmq;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.i18n.I18nLocaleEntity;
import org.twins.core.dao.i18n.I18nLocaleRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.trigger.messaging.rabbitmq.payloads.RabbitMqMessagePayloadTranslation;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.rabbit.AmpqManager;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinTriggerRabbitMqSendI18NFieldValueWithLocaleTest extends BaseUnitTest {

    @Mock
    private AmpqManager ampqManager;

    @Mock
    private I18nLocaleRepository i18nLocaleRepository;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    @Mock
    private AuthService authService;

    private TwinTriggerRabbitMqSendI18NFieldValueWithLocale trigger;

    @BeforeEach
    void setUp() throws Exception {
        trigger = new TwinTriggerRabbitMqSendI18NFieldValueWithLocale(ampqManager, i18nLocaleRepository, authService);
        setField(trigger, "twinClassFieldService", twinClassFieldService);
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

    private I18nLocaleEntity buildLocale(String locale, boolean active) {
        var entity = new I18nLocaleEntity();
        entity.setLocale(locale);
        entity.setActive(active);
        return entity;
    }

    private TwinClassFieldEntity buildField(UUID id, String key) {
        var field = new TwinClassFieldEntity();
        field.setId(id);
        field.setKey(key);
        return field;
    }

    @Nested
    class Send {

        @Test
        void send_sendsTranslationPayloadWithCorrectFields() throws ServiceException {
            var twin = buildTwin();
            var userId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            var businessAccountId = UUID.randomUUID();
            var jobTwinId = UUID.randomUUID();
            var fieldId1 = UUID.randomUUID();

            var props = new Properties();
            props.setProperty("url", "amqp://localhost:5675");
            props.setProperty("exchange", "ex");
            props.setProperty("queue", "q");
            props.setProperty("operation", "TRANSLATE");
            props.setProperty("fields", fieldId1.toString());
            props.setProperty("src_locale", "en");

            var apiUser = mock(ApiUser.class);
            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getUserId()).thenReturn(userId);
            when(apiUser.getDomainId()).thenReturn(domainId);
            when(apiUser.getBusinessAccountId()).thenReturn(businessAccountId);

            var locales = List.of(
                    buildLocale("en", true),
                    buildLocale("fr", true),
                    buildLocale("de", true)
            );
            when(i18nLocaleRepository.findAllByActiveIsTrue()).thenReturn(locales);

            var tcField = buildField(fieldId1, "description");
            when(twinClassFieldService.findEntitiesSafe(any(Set.class)))
                    .thenReturn(new Kit<>(List.of(tcField), TwinClassFieldEntity::getId));

            var mockFactory = mock(CachingConnectionFactory.class);
            TwinTriggerRabbitMqConnection.rabbitConnectionCache.put("amqp://localhost:5675", mockFactory);

            trigger.send(props, twin, null, null, jobTwinId);

            var captor = ArgumentCaptor.forClass(RabbitMqMessagePayloadTranslation.class);
            verify(ampqManager).sendMessage(eq(mockFactory), eq("ex"), eq("q"), captor.capture());

            var payload = captor.getValue();
            assertEquals(twin.getId(), payload.twinsId());
            assertEquals(userId, payload.userId());
            assertEquals(businessAccountId, payload.businessAccountId());
            assertEquals(domainId, payload.domainId());
            assertEquals("TRANSLATE", payload.operation());
            assertEquals(jobTwinId, payload.jobTwinId());
            assertEquals(1, payload.fields().size());

            var fieldInfo = payload.fields().get(0);
            assertEquals("description", fieldInfo.fieldKey());
            assertEquals("en", fieldInfo.sourceLanguage());
            assertEquals(2, fieldInfo.targetLanguages().size());
            assertTrue(fieldInfo.targetLanguages().contains("fr"));
            assertTrue(fieldInfo.targetLanguages().contains("de"));
        }
    }
}
