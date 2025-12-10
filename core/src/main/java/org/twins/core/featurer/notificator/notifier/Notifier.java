package org.twins.core.featurer.notificator.notifier;

import alcosi.notification_manager.v1.ReceiverServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamWordList;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.HistoryNotificationSchemaMapEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@FeaturerType(id = FeaturerTwins.TYPE_48,
        name = "Notifier",
        description = "")
@Slf4j
public abstract class Notifier extends FeaturerTwins {

    @FeaturerParam(name = "hostDomainBaseUri", optional = true, defaultValue = "/")
    public static final FeaturerParamString hostDomainBaseUri = new FeaturerParamString("hostDomainBaseUri");

    @FeaturerParam(name = "requiredParamKeys", optional = true, defaultValue = "PARAM_KEY")
    public static final FeaturerParamWordList requiredParamKeys = new FeaturerParamWordList("requiredParamKeys");

    protected final Map<String, Object> stubCache = new ConcurrentHashMap<>();

    public void notify(List<HistoryEntity> historyList, HistoryNotificationSchemaMapEntity schemaMap, HashMap<String, String> notifierParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, notifierParams, new HashMap<>());
        notify(historyList, schemaMap, properties);
    }

    protected abstract void notify(List<HistoryEntity> historyList, HistoryNotificationSchemaMapEntity schemaMap, Properties properties) throws ServiceException;

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
