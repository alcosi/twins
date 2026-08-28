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
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.trigger.messaging.rabbitmq.payloads.RabbitMqMessagePayloadTwin;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.rabbit.AmpqManager;
import org.twins.core.service.twin.TwinSearchService;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinTriggerRabbitMqSendTwinChildrenInStatusesTest extends BaseUnitTest {

    @Mock
    private AmpqManager ampqManager;

    @Mock
    private AuthService authService;

    @Mock
    private TwinSearchService twinSearchService;

    private TwinTriggerRabbitMqSendTwinChildrenInStatuses trigger;

    @BeforeEach
    void setUp() throws Exception {
        trigger = new TwinTriggerRabbitMqSendTwinChildrenInStatuses(ampqManager, authService, twinSearchService);
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

    @Nested
    class Send {

        @Test
        void send_withChildren_sendsMessageForEachChild() throws ServiceException {
            var parentTwin = buildTwin();
            var child1 = buildTwin();
            var child2 = buildTwin();
            var userId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            var businessAccountId = UUID.randomUUID();
            var jobTwinId = UUID.randomUUID();
            var statusId1 = UUID.randomUUID();

            var props = new Properties();
            props.setProperty("url", "amqp://localhost:5676");
            props.setProperty("exchange", "ex");
            props.setProperty("queue", "q");
            props.setProperty("operation", "BATCH");
            props.setProperty("childrenTwinStatusIdList", statusId1.toString());
            props.setProperty("childrenTwinClassIdList", "");
            props.setProperty("exclude", "false");

            var apiUser = mock(ApiUser.class);
            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getUserId()).thenReturn(userId);
            when(apiUser.getDomainId()).thenReturn(domainId);
            when(apiUser.getBusinessAccountId()).thenReturn(businessAccountId);

            when(twinSearchService.findTwins(any(BasicSearch.class)))
                    .thenReturn(List.of(child1, child2));

            var mockFactory = mock(CachingConnectionFactory.class);
            TwinTriggerRabbitMqConnection.rabbitConnectionCache.put("amqp://localhost:5676", mockFactory);

            trigger.send(props, parentTwin, null, null, jobTwinId);

            var captor = ArgumentCaptor.forClass(RabbitMqMessagePayloadTwin.class);
            verify(ampqManager, times(2)).sendMessage(eq(mockFactory), eq("ex"), eq("q"), captor.capture());

            var payloads = captor.getAllValues();
            assertEquals(2, payloads.size());
            assertEquals(child1.getId(), payloads.get(0).twinsId());
            assertEquals(child2.getId(), payloads.get(1).twinsId());
        }

        @Test
        void send_withNoChildren_sendsNoMessages() throws ServiceException {
            var parentTwin = buildTwin();
            var jobTwinId = UUID.randomUUID();
            var statusId1 = UUID.randomUUID();

            var props = new Properties();
            props.setProperty("url", "amqp://localhost:5677");
            props.setProperty("exchange", "ex");
            props.setProperty("queue", "q");
            props.setProperty("operation", "BATCH");
            props.setProperty("childrenTwinStatusIdList", statusId1.toString());
            props.setProperty("childrenTwinClassIdList", "");
            props.setProperty("exclude", "false");

            var apiUser = mock(ApiUser.class);
            when(authService.getApiUser()).thenReturn(apiUser);

            when(twinSearchService.findTwins(any(BasicSearch.class)))
                    .thenReturn(Collections.emptyList());

            var mockFactory = mock(CachingConnectionFactory.class);
            TwinTriggerRabbitMqConnection.rabbitConnectionCache.put("amqp://localhost:5677", mockFactory);

            trigger.send(props, parentTwin, null, null, jobTwinId);

            verify(ampqManager, never()).sendMessage(any(), anyString(), anyString(), any());
        }

        @Test
        void send_buildsBasicSearchWithCorrectParameters() throws ServiceException {
            var parentTwin = buildTwin();
            var child = buildTwin();
            var statusId1 = UUID.randomUUID();
            var statusId2 = UUID.randomUUID();
            var classId = UUID.randomUUID();

            var props = new Properties();
            props.setProperty("url", "amqp://localhost:5678");
            props.setProperty("exchange", "ex");
            props.setProperty("queue", "q");
            props.setProperty("operation", "TEST");
            props.setProperty("childrenTwinStatusIdList", statusId1 + "," + statusId2);
            props.setProperty("childrenTwinClassIdList", classId.toString());
            props.setProperty("exclude", "true");

            var apiUser = mock(ApiUser.class);
            when(authService.getApiUser()).thenReturn(apiUser);

            when(twinSearchService.findTwins(any(BasicSearch.class)))
                    .thenReturn(List.of(child));

            var mockFactory = mock(CachingConnectionFactory.class);
            TwinTriggerRabbitMqConnection.rabbitConnectionCache.put("amqp://localhost:5678", mockFactory);

            trigger.send(props, parentTwin, null, null, null);

            var searchCaptor = ArgumentCaptor.forClass(BasicSearch.class);
            verify(twinSearchService).findTwins(searchCaptor.capture());

            var search = searchCaptor.getValue();
            assertEquals(Set.of(parentTwin.getId()), search.getHeadTwinIdList(), "head twin id should match");
            assertEquals(Set.of(statusId1, statusId2), search.getStatusIdExcludeList(), "status ids should be in exclude list when exclude=true");
            assertEquals(Set.of(classId), search.getTwinClassExtendsHierarchyContainsIdList(), "class id should be in hierarchy contains list");
        }
    }
}
