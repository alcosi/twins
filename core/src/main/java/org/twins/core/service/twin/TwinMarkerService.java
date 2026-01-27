package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.KitUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinMarkerEntity;
import org.twins.core.dao.twin.TwinMarkerRepository;
import org.twins.core.dao.twinclass.TwinClassDynamicMarkerEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.EntityRelinkOperation;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.enums.EntityRelinkOperationStrategy;
import org.twins.core.enums.twin.LoadState;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.twinclass.TwinClassDynamicMarkerService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinMarkerService extends EntitySecureFindServiceImpl<TwinMarkerEntity> {
    final TwinMarkerRepository twinMarkerRepository;
    final TwinService twinService;
    final DataListService dataListService;
    final DataListOptionService dataListOptionService;
    final TwinClassDynamicMarkerService twinClassDynamicMarkerService;
    final TwinValidatorSetService twinValidatorSetService;

    @Override
    public CrudRepository<TwinMarkerEntity, UUID> entityRepository() {
        return twinMarkerRepository;
    }

    @Override
    public Function<TwinMarkerEntity, UUID> entityGetIdFunction() {
        return TwinMarkerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinMarkerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinMarkerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinId");
        if (entity.getMarkerDataListOptionId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinStatusId");
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getTwin() == null)
                    entity.setTwin(twinService.findEntitySafe(entity.getTwinId()));
                if (entity.getMarkerDataListOption() == null)
                    entity.setMarkerDataListOption(dataListService.findDataListOption(entity.getMarkerDataListOptionId()));
            default:
                UUID expectedDataListId = entity.getTwin().getTwinClass().getMarkerDataListId() != null
                        ? entity.getTwin().getTwinClass().getMarkerDataListId()
                        : entity.getTwin().getTwinClass().getInheritedMarkerDataListId();

                if (!expectedDataListId.equals(entity.getMarkerDataListOption().getDataListId()))
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect twinMarker dataListOptionId[" + entity.getMarkerDataListOptionId() + "]");
        }
        return true;
    }

    public List<TwinMarkerEntity> findByTwinId(UUID twinId) {
        return twinMarkerRepository.findByTwinId(twinId);
    }

    public Kit<DataListOptionEntity, UUID> loadMarkers(TwinEntity twinEntity) throws ServiceException {
        loadMarkers(Collections.singletonList(twinEntity));
        return twinEntity.getTwinMarkerKit();
    }

    public void loadMarkers(Collection<TwinEntity> twinEntityList) throws ServiceException {
        List<TwinEntity> twinsToLoad = filterItemsForLoad(twinEntityList, TwinEntity::getMarkersLoadState, LoadState.LOADING, LoadState.NOT_LOADED);
        if (twinsToLoad.isEmpty()) return;

        setEntityEnumState(twinsToLoad, TwinEntity::setMarkersLoadState, LoadState.LOADING);

        try {
            loadStaticMarkers(twinsToLoad);
            loadDynamicMarkers(twinsToLoad);
            setEntityEnumState(twinsToLoad, TwinEntity::setMarkersLoadState, LoadState.LOADED);

        } catch (ServiceException e) {
            setEntityEnumState(twinsToLoad, TwinEntity::setMarkersLoadState, LoadState.NOT_LOADED);
            throw e;
        }
    }

    private void loadStaticMarkers(List<TwinEntity> twinsToLoad) throws ServiceException {
        Kit<TwinEntity, UUID> needLoadStatic = new Kit<>(TwinEntity::getId);
        twinsToLoad.stream()
                .filter(twin -> twin.getTwinMarkerKit() == null)
                .forEach(needLoadStatic::add);

        if (needLoadStatic.isEmpty()) {
            return;
        }

        KitGrouped<TwinMarkerEntity, UUID, UUID> markersByTwin = new KitGrouped<>(
                twinMarkerRepository.findByTwinIdIn(needLoadStatic.getIdSet()),
                TwinMarkerEntity::getId,
                TwinMarkerEntity::getTwinId);


        if (markersByTwin.isNotEmpty()) {
            assignStaticMarkers(needLoadStatic, markersByTwin);
        } else {
            assignEmptyMarkers(needLoadStatic.getCollection());
        }
    }

    private void assignStaticMarkers(Kit<TwinEntity, UUID> needLoadStatic, KitGrouped<TwinMarkerEntity, UUID, UUID> markersByTwin) {
        for (TwinEntity twin : needLoadStatic) {
            twin.setTwinMarkerKit(new Kit<>(
                    markersByTwin.getGrouped(twin.getId()).stream()
                            .map(TwinMarkerEntity::getMarkerDataListOption)
                            .collect(Collectors.toList()),
                    DataListOptionEntity::getId));
        }
    }

    private void assignEmptyMarkers(Collection<TwinEntity> twins) {
        for (TwinEntity twinEntity : twins) {
            twinEntity.setTwinMarkerKit(new Kit<>(new ArrayList<>(), DataListOptionEntity::getId));
        }
    }

    private void loadDynamicMarkers(List<TwinEntity> twinsToLoad) throws ServiceException {
        List<TwinEntity> filtered = twinsToLoad.stream().filter(twin -> twin.getTwinClass().getHasDynamicMarkers()).toList();
        if (filtered.isEmpty()) return;

        KitGrouped<TwinEntity, UUID, UUID> twinsByClass = new KitGrouped<>(filtered, TwinEntity::getId, TwinEntity::getTwinClassId);

        KitGrouped<TwinClassDynamicMarkerEntity, UUID, UUID> markersByValidatorSet = new KitGrouped<>(
                twinClassDynamicMarkerService.findByTwinClassIdIn(twinsByClass.getGroupedKeySet()),
                TwinClassDynamicMarkerEntity::getId,
                TwinClassDynamicMarkerEntity::getTwinValidatorSetId);

        twinValidatorSetService.loadTwinValidatorSet(markersByValidatorSet.getCollection());

        for (TwinClassDynamicMarkerEntity twinClassDynamicMarkerEntity : markersByValidatorSet.getCollection()) { //todo perhaps we could iterate by validatorSet in future
            processValidatorSet(twinClassDynamicMarkerEntity, twinsByClass.getGrouped(twinClassDynamicMarkerEntity.getTwinClassId()));
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

    public boolean hasMarker(TwinEntity twinEntity, UUID marker) throws ServiceException {
        Kit<DataListOptionEntity, UUID> markers = loadMarkers(twinEntity);
        return KitUtils.isNotEmpty(markers) && markers.getIdSet().contains(marker);
    }

    public void addMarkers(TwinEntity twinEntity, Set<UUID> markersAdd, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (CollectionUtils.isEmpty(markersAdd))
            return;
        List<TwinMarkerEntity> existingMarkers = twinMarkerRepository.findByTwinId(twinEntity.getId());
        Set<UUID> existingMarkerIds = existingMarkers.stream()
                .map(TwinMarkerEntity::getMarkerDataListOptionId)
                .collect(Collectors.toSet());

        for (UUID marker : markersAdd) {
            if (!existingMarkerIds.contains(marker)) {
                TwinMarkerEntity twinMarkerEntity = new TwinMarkerEntity()
                        .setTwinId(twinEntity.getId())
                        .setTwin(twinEntity)
                        .setMarkerDataListOptionId(marker);
                validateEntityAndThrow(twinMarkerEntity, EntitySmartService.EntityValidateMode.beforeSave);
                //todo add history
                twinChangesCollector.add(twinMarkerEntity);
            }
        }
    }

    public void deleteMarkers(TwinEntity twinEntity, Set<UUID> markersDelete, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (CollectionUtils.isEmpty(markersDelete))
            return;
        // it's not possible to delete it in such way, because we need to write history
        // twinMarkerRepository.deleteByTwinIdAndMarkerDataListOptionIdIn(twinEntity.getId(), markersDelete);
        List<TwinMarkerEntity> markers = twinMarkerRepository.findByTwinIdAndMarkerDataListOptionIdIn(twinEntity.getId(), markersDelete);
        if (markers.size() != markersDelete.size()) {
            log.warn("Mismatch markers for deletion with existing for twin(id: {}) : markers (optionIDs: {}) and markersDelete (optionIDs: {}).",
                    twinEntity.getId(),
                    markers.stream().map(TwinMarkerEntity::getMarkerDataListOptionId).collect(Collectors.toSet()),
                    markersDelete);
        }
        //todo add history
        for (TwinMarkerEntity marker : markers)
            twinChangesCollector.delete(marker);

    }

    @Transactional
    public void deleteAllMarkersForTwinsOfClass(UUID twinClassId) {
        //todo write history
        twinMarkerRepository.deleteByTwin_TwinClassId(twinClassId);
    }

    public Set<UUID> findExistedTwinMarkersForTwinsOfClass(UUID twinClassId) {
        return twinMarkerRepository.findDistinctMakersDataListOptionIdByTwinTwinClassId(twinClassId);
    }

    @Transactional
    public void replaceMarkersForTwinsOfClass(TwinClassEntity twinClassEntity, EntityRelinkOperation entityRelinkOperation) throws ServiceException {
        if (UuidUtils.isNullifyMarker(entityRelinkOperation.getNewId())) {
            //we have to delete all markers from twins of given class
            deleteAllMarkersForTwinsOfClass(twinClassEntity.getId());
            twinClassEntity
                    .setMarkerDataListId(null)
                    .setMarkerDataList(null);
            return;
        }
        DataListEntity newMarkerDataList = dataListService.findEntitySafe(entityRelinkOperation.getNewId());
        //we will try to replace markers with new provided values
        Set<UUID> existedTwinMarkerIds = findExistedTwinMarkersForTwinsOfClass(twinClassEntity.getId());
        if (CollectionUtils.isEmpty(existedTwinMarkerIds)) {
            twinClassEntity
                    .setMarkerDataList(newMarkerDataList)
                    .setMarkerDataListId(newMarkerDataList.getId());
            return; // nice :) we have nothing to do
        }

        if (entityRelinkOperation.getStrategy() == EntityRelinkOperationStrategy.restrict
                && MapUtils.isEmpty(entityRelinkOperation.getReplaceMap()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide markersReplaceMap for markers: " + org.cambium.common.util.StringUtils.join(existedTwinMarkerIds));

        dataListService.loadDataListOptions(newMarkerDataList);
        Set<UUID> markersForDeletion = new HashSet<>();
        for (UUID markerForReplace : existedTwinMarkerIds) {
            if (newMarkerDataList.getOptions().get(markerForReplace) != null) //be smart if somehow already existed marker belongs to new list
                continue;
            UUID replacement = entityRelinkOperation.getReplaceMap().get(markerForReplace);
            if (replacement == null) {
                if (entityRelinkOperation.getStrategy() == EntityRelinkOperationStrategy.restrict)
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide markersReplaceMap value for marker: " + markerForReplace);
                else
                    replacement = UuidUtils.NULLIFY_MARKER;
            }
            if (UuidUtils.isNullifyMarker(replacement)) {
                markersForDeletion.add(markerForReplace);
                continue;
            }
            if (newMarkerDataList.getOptions().get(replacement) == null)
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide correct markersReplaceMap value for marker: " + markerForReplace);
            twinMarkerRepository.replaceMarkersForTwinsOfClass(twinClassEntity.getId(), markerForReplace, replacement);
        }
        if (CollectionUtils.isNotEmpty(markersForDeletion)) {
            twinMarkerRepository.deleteByTwin_TwinClassIdAndMarkerDataListOptionIdIn(twinClassEntity.getId(), markersForDeletion);
        }
        twinClassEntity
                .setMarkerDataList(newMarkerDataList)
                .setMarkerDataListId(newMarkerDataList.getId());
    }
}
