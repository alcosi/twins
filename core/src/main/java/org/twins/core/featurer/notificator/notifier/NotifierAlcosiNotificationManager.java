package org.twins.core.featurer.notificator.notifier;

import alcosi.notification_manager.v1.AlcosiReceiver;
import alcosi.notification_manager.v1.ReceiverServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUrl;
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

    @FeaturerParam(name = "Host domain base uri", order = 1, optional = false, defaultValue = "/")
    public static final FeaturerParamUrl hostDomainBaseUri = new FeaturerParamUrl("hostDomainBaseUri");

    @FeaturerParam(name = "Collect company key", order = 2, optional = true, defaultValue = "COMPANY_ID")
    public static final FeaturerParamString collectCompanyKey = new FeaturerParamString("collectCompanyKey");;

    @FeaturerParam(name = "Collect event key", order = 3, optional = true, defaultValue = "EVENT_ID")
    public static final FeaturerParamString collectEventKey = new FeaturerParamString("collectEventKey");

    @Override
    protected void notify(Set<UUID> recipientIds, Map<String, String> context, String eventCode, Properties properties) throws ServiceException {
        String businessAccountKey = collectCompanyKey.extract(properties);
        context.put(collectEventKey.extract(properties), eventCode);

        String hostDomainBaseUriValue = getHostDomainBaseUri(properties);

        ReceiverServiceGrpc.ReceiverServiceBlockingStub receiverServiceFutureStub = getOrCreateStub(hostDomainBaseUriValue);

        AlcosiReceiver.SendNotificationCommand notificationCommand = AlcosiReceiver.SendNotificationCommand.newBuilder()
                .addAllUsersIds(recipientIds.stream().map(UUID::toString).collect(Collectors.toList()))
                .putFilters(businessAccountKey, context.get(businessAccountKey))
                .setEventId(eventCode)
                .putAllData(context)
                .build();

        receiverServiceFutureStub.sendNotification(notificationCommand);
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
    protected ReceiverServiceGrpc.ReceiverServiceBlockingStub getOrCreateStub(String hostDomainBaseUri) {
        return (ReceiverServiceGrpc.ReceiverServiceBlockingStub) stubCache.computeIfAbsent(hostDomainBaseUri, uri -> {
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

                return ReceiverServiceGrpc.newBlockingStub(channel);//todo .newFutureStub asin
            } catch (Exception e) {
                throw new RuntimeException("Failed to create gRPC stub for URI: " + uri, e);
            }
        });
    }
}
