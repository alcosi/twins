package org.twins.core.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.featurer.notificator.context.ContextCollector;
import org.twins.core.featurer.notificator.notifier.Notifier;
import org.twins.core.featurer.notificator.recipient.RecipientResolver;
import org.twins.core.service.domain.DomainBusinessAccountService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class NotificationService {
    private final FeaturerService featurerService;
    private final DomainBusinessAccountService domainBusinessAccountService;
    private final HistoryNotificationSchemaMapEntityRepository historyNotificationSchemaMapEntityRepository;

    //todo on scheduler
    @Transactional
    public void collect() throws ServiceException {
        //todo check loading many entries and during processing call scheduler
        List<HistoryEntity> historyList = new ArrayList<>(); //todo get histories in statuses NOT SEND

        Map<UUID, List<HistoryEntity>> groupedByBusinessAccount = historyList.stream()
                .collect(Collectors.groupingBy(
                        history -> Optional.ofNullable(history.getTwin())
                                .map(TwinEntity::getOwnerBusinessAccountId)
                                .orElse(null),
                        HashMap::new,
                        Collectors.toList()
                ));

        KitGrouped<DomainBusinessAccountEntity, UUID, UUID> groupedBusinessAccountBySchemaIdKit = new KitGrouped<>(
                domainBusinessAccountService.findEntitiesSafe(groupedByBusinessAccount.keySet()).getCollection(),
                DomainBusinessAccountEntity::getId,
                DomainBusinessAccountEntity::getNotificationSchemaId
        );

        Set<String> historyTypes = historyList.stream()
                .map(HistoryEntity::getHistoryType)
                .map(HistoryType::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<HistoryNotificationSchemaMapEntity> notificationSchemaMap = historyNotificationSchemaMapEntityRepository
                .findAllByHistoryTypeIdInAndNotificationSchemaIdIn(historyTypes, groupedBusinessAccountBySchemaIdKit.getGroupedKeySet());


        for (HistoryNotificationSchemaMapEntity schemaMap : notificationSchemaMap) {
            Set<UUID> recipientIds = recipientResolve(schemaMap.getHistoryNotificationRecipient(), history);
            if (recipientIds.isEmpty())
                continue;

            Map<String, String> contextMap = collectHistoryContext(
                    schemaMap.getNotificationChannelEvent().getHistoryNotificationContext(),
                    new HashMap<>(),
                    history
            );

            NotificationChannelEntity notificationChannel = schemaMap.getNotificationChannelEvent().getNotificationChannel();
            Notifier notifier = featurerService.getFeaturer(notificationChannel.getNotifierFeaturer(), Notifier.class);
            notifier.notify(recipientIds, contextMap, eventCode, businessAccountId, notificationChannel.getNotifierParams());
        }
    }

    private Set<UUID> recipientResolve(HistoryNotificationRecipientEntity notificationRecipient, HistoryEntity history) throws ServiceException {
        RecipientResolver recipientResolver = featurerService.getFeaturer(notificationRecipient.getRecipientResolverFeaturer(), RecipientResolver.class);
        return recipientResolver.resolve(history, notificationRecipient.getRecipientResolverParams());
    }

    private Map<String, String> collectHistoryContext(HistoryNotificationContextEntity historyNotificationContext, Map<String, String> contextMap, HistoryEntity history) throws ServiceException {
        for (HistoryNotificationContextCollectorEntity contextCollector : historyNotificationContext.getContextCollectors()) {
            ContextCollector collector = featurerService.getFeaturer(contextCollector.getContextCollectorFeaturer(), ContextCollector.class);
            contextMap = collector.collectData(history, contextMap, contextCollector.getContextCollectorParams());
        }
        return contextMap;
    }
}
