package org.twins.core.featurer.notificator.notifier;

import alcosi.notification_manager.v1.Receiver;
import alcosi.notification_manager.v1.ReceiverServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.net.URI;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Featurer(id = FeaturerTwins.ID_4801,
        name = "Notifier Alcosi Notification Manager",
        description = "")
public class NotifierAlcosiNotificationManager extends Notifier {

    @FeaturerParam(name = "hostDomainBaseUri", optional = true, defaultValue = "/")
    public static final FeaturerParamString hostDomainBaseUri = new FeaturerParamString("hostDomainBaseUri");

    @Override
    protected void notify(Set<UUID> recipientIds, Map<String, String> context, String eventCode, UUID businessAccountId, Properties properties) throws ServiceException {
        fillBaseDataMap(context, businessAccountId, eventCode);

        String hostDomainBaseUriValue = getHostDomainBaseUri(properties);

        ReceiverServiceGrpc.ReceiverServiceFutureStub receiverServiceFutureStub = getOrCreateStub(hostDomainBaseUriValue);

        Receiver.SendNotificationCommand notificationCommand = Receiver.SendNotificationCommand.newBuilder()
                .addAllUsersIds(recipientIds.stream().map(UUID::toString).collect(Collectors.toList()))
                .putFilters("COMPANY_ID", businessAccountId.toString())
                .setEventId(eventCode)
                .putAllData(context)
                .build();

        receiverServiceFutureStub.sendNotification(notificationCommand);
    }

    private void fillBaseDataMap(Map<String, String> contextMap, UUID businessAccountId, String eventCode) {
        contextMap.put("COMPANY_ID", businessAccountId.toString());
        contextMap.put("EVENT_ID", eventCode);
    }

    protected String getHostDomainBaseUri(Properties properties) throws ServiceException {
        String hostDomainBaseUriValue = hostDomainBaseUri.extract(properties);
        if (hostDomainBaseUriValue == null || hostDomainBaseUriValue.isEmpty() || "/".equals(hostDomainBaseUriValue)) {
            throw new ServiceException(org.cambium.featurer.exception.ErrorCodeFeaturer.INCORRECT_CONFIGURATION,
                    "hostDomainBaseUri parameter is required for " + this.getClass().getSimpleName());
        }
        return hostDomainBaseUriValue;
    }

    @SuppressWarnings("unchecked")
    protected ReceiverServiceGrpc.ReceiverServiceFutureStub getOrCreateStub(String hostDomainBaseUri) {
        return (ReceiverServiceGrpc.ReceiverServiceFutureStub) stubCache.computeIfAbsent(hostDomainBaseUri, uri -> {
            try {
                String target;
                if (uri.startsWith("http://") || uri.startsWith("https://")) {
                    URI parsedUri = URI.create(uri);
                    target = parsedUri.getHost() + ":" + (parsedUri.getPort() != -1 ? parsedUri.getPort() : 20108);
                } else {
                    target = uri;
                }

                ManagedChannel channel = ManagedChannelBuilder
                        .forTarget(target)
                        .usePlaintext()
                        .build();

                return ReceiverServiceGrpc.newFutureStub(channel);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create gRPC stub for URI: " + uri, e);
            }
        });
    }
}
