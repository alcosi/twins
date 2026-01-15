package org.twins.core.service.notification;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.notification.HistoryNotificationRecipientCollectorEntity;
import org.twins.core.dao.notification.HistoryNotificationRecipientCollectorRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.notification.HistoryNotificationRecipientCollectorCreate;
import org.twins.core.domain.notification.HistoryNotificationRecipientCollectorUpdate;
import org.twins.core.featurer.notificator.recipient.RecipientResolver;
import org.twins.core.mappers.rest.notification.HistoryNotificationRecipientCollectorUpdateDTOReverseMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Lazy
public class HistoryNotificationRecipientCollectorService extends EntitySecureFindServiceImpl<HistoryNotificationRecipientCollectorEntity> {
    private final HistoryNotificationRecipientCollectorRepository repository;
    private final HistoryNotificationRecipientCollectorUpdateDTOReverseMapper updateDTOReverseMapper;
    @Lazy
    private final FeaturerService featurerService;

    @Override
    public CrudRepository<HistoryNotificationRecipientCollectorEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<HistoryNotificationRecipientCollectorEntity, UUID> entityGetIdFunction() {
        return HistoryNotificationRecipientCollectorEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(HistoryNotificationRecipientCollectorEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(HistoryNotificationRecipientCollectorEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<HistoryNotificationRecipientCollectorEntity> createRecipientCollectors(List<HistoryNotificationRecipientCollectorCreate> recipientCollectors) throws ServiceException {
        if (recipientCollectors == null || recipientCollectors.isEmpty()) {
            return Collections.emptyList();
        }

        List<HistoryNotificationRecipientCollectorEntity> recipientCollectorsToSave = new ArrayList<>();

        for (HistoryNotificationRecipientCollectorCreate recipientCollector : recipientCollectors) {
            HashMap<String, String> recipientResolverParams = new HashMap<>(recipientCollector.getRecipientResolverParams());
            if (recipientCollector.getRecipientResolverFeaturerId() != null) {
                featurerService.checkValid(recipientCollector.getRecipientResolverFeaturerId(), recipientResolverParams, RecipientResolver.class);
                featurerService.prepareForStore(recipientCollector.getRecipientResolverFeaturerId(), recipientResolverParams);
            }

            HistoryNotificationRecipientCollectorEntity recipientEntity = new HistoryNotificationRecipientCollectorEntity()
                    .setHistoryNotificationRecipientId(recipientCollector.getRecipientId())
                    .setRecipientResolverFeaturerId(recipientCollector.getRecipientResolverFeaturerId())
                    .setRecipientResolverParams(recipientResolverParams)
                    .setExclude(recipientCollector.getExclude());

            recipientCollectorsToSave.add(recipientEntity);
        }

        return StreamSupport.stream(saveSafe(recipientCollectorsToSave).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<HistoryNotificationRecipientCollectorEntity> updateRecipientCollectors(List<HistoryNotificationRecipientCollectorUpdate> recipientCollectors) throws ServiceException {
        if (recipientCollectors == null || recipientCollectors.isEmpty()) {
            return Collections.emptyList();
        }

        ChangesHelperMulti<HistoryNotificationRecipientCollectorEntity> changes = new ChangesHelperMulti<>();
        List<HistoryNotificationRecipientCollectorEntity> allEntities = new ArrayList<>(recipientCollectors.size());

        Kit<HistoryNotificationRecipientCollectorEntity, UUID> entitiesKit = findEntitiesSafe(recipientCollectors.stream().map(HistoryNotificationRecipientCollectorUpdate::getId).toList());

        for (HistoryNotificationRecipientCollectorUpdate recipientCollector : recipientCollectors) {
            HistoryNotificationRecipientCollectorEntity entity = entitiesKit.get(recipientCollector.getId());
            allEntities.add(entity);

            ChangesHelper changesHelper = new ChangesHelper();

            updateEntityFieldByValue(recipientCollector.getRecipientId(), entity,
                    HistoryNotificationRecipientCollectorEntity::getHistoryNotificationRecipientId, HistoryNotificationRecipientCollectorEntity::setHistoryNotificationRecipientId,
                    HistoryNotificationRecipientCollectorEntity.Fields.historyNotificationRecipientId, changesHelper);
            updateEntityFieldByValue(recipientCollector.getExclude(), entity,
                    HistoryNotificationRecipientCollectorEntity::getExclude, HistoryNotificationRecipientCollectorEntity::setExclude,
                    HistoryNotificationRecipientCollectorEntity.Fields.exclude, changesHelper);
            updateFieldRecipientResolverFeaturerId(entity, recipientCollector.getRecipientResolverFeaturerId(), new HashMap<>(recipientCollector.getRecipientResolverParams()),changesHelper);

            changes.add(entity, changesHelper);
        }

        updateSafe(changes);

        return allEntities;
    }

    public void updateFieldRecipientResolverFeaturerId(HistoryNotificationRecipientCollectorEntity dbHistoryNotificationRecipientCollectorEntity, Integer newFeaturerId, HashMap<String, String> newFeaturerParams, ChangesHelper changesHelper) throws ServiceException {
        if (newFeaturerId == null || newFeaturerId == 0) {
            if (MapUtils.isEmpty(newFeaturerParams))
                return; //nothing was changed
            else
                newFeaturerId = dbHistoryNotificationRecipientCollectorEntity.getRecipientResolverFeaturerId(); // only params where changed
        }
        if (changesHelper.isChanged(HistoryNotificationRecipientCollectorEntity.Fields.recipientResolverFeaturerId, dbHistoryNotificationRecipientCollectorEntity.getRecipientResolverFeaturerId(), newFeaturerId)) {
            featurerService.checkValid(newFeaturerId, newFeaturerParams, RecipientResolver.class);
            dbHistoryNotificationRecipientCollectorEntity
                    .setRecipientResolverFeaturerId(newFeaturerId);
        }
        featurerService.prepareForStore(newFeaturerId, newFeaturerParams);
        if (!MapUtils.areEqual(dbHistoryNotificationRecipientCollectorEntity.getRecipientResolverParams(), newFeaturerParams)) {
            changesHelper.add(TwinClassFieldEntity.Fields.fieldTyperParams, dbHistoryNotificationRecipientCollectorEntity.getRecipientResolverParams(), newFeaturerParams);
            dbHistoryNotificationRecipientCollectorEntity
                    .setRecipientResolverParams(newFeaturerParams);
        }
    }

    public void loadRecipientResolverFeaturer(HistoryNotificationRecipientCollectorEntity entity) {
        loadRecipientResolverFeaturer(List.of(entity));
    }

    public void loadRecipientResolverFeaturer(Collection<HistoryNotificationRecipientCollectorEntity> entities) {
        featurerService.loadFeaturers(entities,
                HistoryNotificationRecipientCollectorEntity::getId,
                HistoryNotificationRecipientCollectorEntity::getRecipientResolverFeaturerId,
                HistoryNotificationRecipientCollectorEntity::getRecipientResolverFeaturer,
                HistoryNotificationRecipientCollectorEntity::setRecipientResolverFeaturer);
    }

}
