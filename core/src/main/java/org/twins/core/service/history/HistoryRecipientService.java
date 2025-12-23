package org.twins.core.service.history;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.HistoryNotificationRecipientCollectorEntity;
import org.twins.core.dao.notification.HistoryNotificationRecipientCollectorRepository;
import org.twins.core.dao.notification.HistoryNotificationRecipientEntity;
import org.twins.core.dao.notification.HistoryNotificationRecipientRepository;
import org.twins.core.featurer.notificator.recipient.RecipientResolver;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class HistoryRecipientService extends EntitySecureFindServiceImpl<HistoryNotificationRecipientEntity> {
    private final HistoryNotificationRecipientRepository repository;
    private final HistoryNotificationRecipientCollectorRepository historyNotificationRecipientCollectorRepository;
    private final FeaturerService featurerService;

    @Override
    public CrudRepository<HistoryNotificationRecipientEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<HistoryNotificationRecipientEntity, UUID> entityGetIdFunction() {
        return HistoryNotificationRecipientEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(HistoryNotificationRecipientEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(HistoryNotificationRecipientEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public Set<HistoryNotificationRecipientCollectorEntity> getRecipientCollectors(UUID recipientId) {
        //todo perhaps this can be cached
        return historyNotificationRecipientCollectorRepository.findAllByHistoryNotificationRecipientId(recipientId);
    }

    public Set<UUID> recipientResolve(UUID recipientId, HistoryEntity history) throws ServiceException {
        Set<HistoryNotificationRecipientCollectorEntity> collectors = getRecipientCollectors(recipientId);

        Map<Boolean, List<HistoryNotificationRecipientCollectorEntity>> partitioned = collectors.stream()
                        .collect(Collectors.partitioningBy(HistoryNotificationRecipientCollectorEntity::getExclude));

        List<HistoryNotificationRecipientCollectorEntity> includeCollectors = partitioned.get(false);
        List<HistoryNotificationRecipientCollectorEntity> excludeCollectors = partitioned.get(true);

        Set<UUID> include = resolveRecipient(history, includeCollectors);
        Set<UUID> exclude = resolveRecipient(history, excludeCollectors);

        include.removeAll(exclude);
        return include;
    }

    private Set<UUID> resolveRecipient(HistoryEntity history, List<HistoryNotificationRecipientCollectorEntity> collectors) throws ServiceException {
        Set<UUID> result = new HashSet<>();
        for (HistoryNotificationRecipientCollectorEntity collector : collectors) {
            RecipientResolver resolver = featurerService.getFeaturer(collector.getRecipientResolverFeaturerId(), RecipientResolver.class);
            resolver.resolve(history, result, collector.getRecipientResolverParams());
        }
        return result;
    }
}
