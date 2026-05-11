package org.twins.core.service.twinclass;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassDynamicMarkerEntity;
import org.twins.core.dao.twinclass.TwinClassDynamicMarkerRepository;
import org.twins.core.service.twin.TwinValidatorSetService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassDynamicMarkerService extends EntitySecureFindServiceImpl<TwinClassDynamicMarkerEntity> {
    private final TwinClassDynamicMarkerRepository twinClassDynamicMarkerRepository;
    private final TwinValidatorSetService twinValidatorSetService;

    @Lazy
    @Autowired
    TwinClassService twinClassService;

    @Override
    public CrudRepository<TwinClassDynamicMarkerEntity, UUID> entityRepository() {
        return twinClassDynamicMarkerRepository;
    }


    @Override
    public Function<TwinClassDynamicMarkerEntity, UUID> entityGetIdFunction() {
        return TwinClassDynamicMarkerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassDynamicMarkerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return checkDomainAccessDenied(entity.getTwinClass().getDomainId(), entity.logNormal(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(TwinClassDynamicMarkerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassDynamicMarkerEntity> createTwinClassDynamicMarkerList(Collection<TwinClassDynamicMarkerEntity> dynamicMarkers) throws ServiceException {
        if (CollectionUtils.isEmpty(dynamicMarkers)) {
            return Collections.emptyList();
        }

        validateEntitiesAndThrow(dynamicMarkers, EntitySmartService.EntityValidateMode.beforeSave);

        return StreamSupport.stream(entityRepository().saveAll(dynamicMarkers).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassDynamicMarkerEntity> updateTwinClassDynamicMarkerList(Collection<TwinClassDynamicMarkerEntity> dynamicMarkers) throws ServiceException {
        if (CollectionUtils.isEmpty(dynamicMarkers)) {
            return Collections.emptyList();
        }

        Kit<TwinClassDynamicMarkerEntity, UUID> dbTwinClassDynamicMarkerEntityKit = findEntitiesSafe(
                dynamicMarkers.stream()
                        .map(TwinClassDynamicMarkerEntity::getId)
                        .collect(Collectors.toList())
        );

        ChangesHelperMulti<TwinClassDynamicMarkerEntity> changes = new ChangesHelperMulti<>();

        for (TwinClassDynamicMarkerEntity twinClassDynamicMarkerEntity : dynamicMarkers) {
            ChangesHelper changesHelper = new ChangesHelper();
            TwinClassDynamicMarkerEntity dbTwinClassDynamicMarkerEntity = dbTwinClassDynamicMarkerEntityKit.get(twinClassDynamicMarkerEntity.getId());

            updateEntityFieldByEntity(twinClassDynamicMarkerEntity, dbTwinClassDynamicMarkerEntity,
                    TwinClassDynamicMarkerEntity::getTwinClassId, TwinClassDynamicMarkerEntity::setTwinClassId,
                    TwinClassDynamicMarkerEntity.Fields.twinClassId, changesHelper);
            updateEntityFieldByEntity(twinClassDynamicMarkerEntity, dbTwinClassDynamicMarkerEntity,
                    TwinClassDynamicMarkerEntity::getTwinValidatorSetId, TwinClassDynamicMarkerEntity::setTwinValidatorSetId,
                    TwinClassDynamicMarkerEntity.Fields.twinValidatorSetId, changesHelper);
            updateEntityFieldByEntity(twinClassDynamicMarkerEntity, dbTwinClassDynamicMarkerEntity,
                    TwinClassDynamicMarkerEntity::getMarkerDataListOptionId, TwinClassDynamicMarkerEntity::setMarkerDataListOptionId,
                    TwinClassDynamicMarkerEntity.Fields.markerDataListOptionId, changesHelper);

            changes.add(dbTwinClassDynamicMarkerEntity, changesHelper);
        }

        updateSafe(changes);

        return dbTwinClassDynamicMarkerEntityKit.getList();
    }

    public void loadTwinValidatorSet(TwinClassDynamicMarkerEntity entity) throws ServiceException {
        loadTwinValidatorSet(List.of(entity));
    }

    public void loadTwinValidatorSet(Collection<TwinClassDynamicMarkerEntity> entities) throws ServiceException {
        twinValidatorSetService.load(entities,
                TwinClassDynamicMarkerEntity::getId,
                TwinClassDynamicMarkerEntity::getTwinValidatorSetId,
                TwinClassDynamicMarkerEntity::getTwinValidatorSet,
                TwinClassDynamicMarkerEntity::setTwinValidatorSet);
    }

    public void loadDynamicMarkers(List<TwinEntity> twinsToLoad) throws ServiceException {
        if (CollectionUtils.isEmpty(twinsToLoad)) return;

        Set<UUID> twinClassIds = new HashSet<>();
        Set<UUID> extendsClassesSet = new HashSet<>();
        for (var twinEntity : twinsToLoad) {
            twinClassIds.addAll(twinEntity.getTwinClass().getExtendedClassIdSet());
            extendsClassesSet.addAll(twinEntity.getTwinClass().getExtendedClassIdSetExcludeCurrent());
        }

        var loaded = twinClassDynamicMarkerRepository.findByTwinClassIdIn(twinClassIds, extendsClassesSet);
        KitGrouped<TwinClassDynamicMarkerEntity, UUID, UUID> markersByValidatorSet = new KitGrouped<>(
                loaded,
                TwinClassDynamicMarkerEntity::getId,
                TwinClassDynamicMarkerEntity::getTwinValidatorSetId);

        twinValidatorSetService.loadTwinValidatorSet(markersByValidatorSet.getCollection());

        List<TwinEntity> twinsToValidate = new ArrayList<>();
        for (TwinClassDynamicMarkerEntity twinClassDynamicMarkerEntity : markersByValidatorSet.getCollection()) { //todo perhaps we could iterate by validatorSet in future
            twinsToValidate.clear();
            for (var twin : twinsToLoad) {
                if (twinClassService.isInheritedFromClass(twin.getTwinClass(), twinClassDynamicMarkerEntity.getTwinClassId(), twinClassDynamicMarkerEntity.getInheritable())) {
                    twinsToValidate.add(twin);
                }
            }
            processValidatorSet(twinClassDynamicMarkerEntity, twinsToValidate);
        }
    }

    private void processValidatorSet(TwinClassDynamicMarkerEntity dynamicMarkerEntity, List<TwinEntity> twinEntitiesToLoad) throws ServiceException {
        Map<UUID, ValidationResult> validationResults = twinValidatorSetService.isValid(twinEntitiesToLoad, dynamicMarkerEntity);

        for (TwinEntity twin : twinEntitiesToLoad) {
            if (validationResults.get(twin.getId()).isValid()) {
                if (twin.getTwinMarkerKit() == null) {
                    twin.setTwinMarkerKit(new Kit<>(DataListOptionEntity::getId));
                }
                twin.getTwinMarkerKit().add(dynamicMarkerEntity.getMarkerDataListOption());
            }
        }
    }
}
