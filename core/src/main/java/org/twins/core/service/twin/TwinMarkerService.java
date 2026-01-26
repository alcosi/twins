package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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

    public List<DataListOptionEntity> findDataListOptionByTwinId(UUID twinId) {
        return twinMarkerRepository.findDataListOptionByTwinId(twinId);
    }

    public Kit<DataListOptionEntity, UUID> loadMarkers(TwinEntity twinEntity) throws ServiceException {
        if (twinEntity.getMarkersLoadState() == LoadState.LOADING) {
            throw new ServiceException(ErrorCodeTwins.TWIN_MARKER_RECURSIVE_LOAD_DETECTED, "Cannot load markers recursively for twin: " + twinEntity.logShort());
        }

        if (twinEntity.getMarkersLoadState() == LoadState.LOADED)
            return twinEntity.getTwinMarkerKit();

        loadMarkers(Collections.singletonList(twinEntity));
        return twinEntity.getTwinMarkerKit();
    }

    public void loadMarkers(Collection<TwinEntity> twinEntityList) throws ServiceException {
        if (CollectionUtils.isEmpty(twinEntityList)) return;

        List<TwinEntity> twinsWithRecursion = twinEntityList.stream().filter(twin -> twin.getMarkersLoadState() == LoadState.LOADING).toList();

        if (!twinsWithRecursion.isEmpty()) {
            throw new ServiceException(ErrorCodeTwins.TWIN_MARKER_RECURSIVE_LOAD_DETECTED, "Cannot load markers recursively for twin: " + twinsWithRecursion.getFirst().logShort());
        }

        List<TwinEntity> twinsToLoad = twinEntityList.stream().filter(twin -> twin.getMarkersLoadState() == LoadState.NOT_LOAD).toList();

        if (twinsToLoad.isEmpty()) return;

        twinsToLoad.forEach(twin -> twin.setMarkersLoadState(LoadState.LOADING));

        try {
            Map<UUID, TwinEntity> needLoadStatic = new HashMap<>();
            for (TwinEntity twinEntity : twinsToLoad) {
                if (twinEntity.getTwinMarkerKit() == null) {
                    needLoadStatic.put(twinEntity.getId(), twinEntity);
                }
            }

            if (!needLoadStatic.isEmpty()) {
                List<TwinMarkerEntity> twinMarkerEntityList = twinMarkerRepository.findByTwinIdIn(needLoadStatic.keySet());
                if (CollectionUtils.isNotEmpty(twinMarkerEntityList)) {
                    Map<UUID, List<DataListOptionEntity>> markersMap = new HashMap<>();
                    for (TwinMarkerEntity twinMarkerEntity : twinMarkerEntityList) {
                        markersMap.computeIfAbsent(twinMarkerEntity.getTwinId(), k -> new ArrayList<>())
                                .add(twinMarkerEntity.getMarkerDataListOption());
                    }

                    for (Map.Entry<UUID, TwinEntity> entry : needLoadStatic.entrySet()) {
                        List<DataListOptionEntity> twinMarkers = markersMap.get(entry.getKey());
                        List<DataListOptionEntity> markersForKit = twinMarkers != null
                                ? new ArrayList<>(twinMarkers)
                                : new ArrayList<>();
                        entry.getValue().setTwinMarkerKit(new Kit<>(markersForKit, DataListOptionEntity::getId));
                    }
                } else {
                    for (TwinEntity twinEntity : needLoadStatic.values()) {
                        twinEntity.setTwinMarkerKit(new Kit<>(new ArrayList<>(), DataListOptionEntity::getId));
                    }
                }
            }

            List<TwinEntity> twinsWithDynamicMarkers = twinsToLoad.stream()
                    .filter(twin -> twin.getTwinClass().getHasDynamicMarkers())
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(twinsWithDynamicMarkers)) {
                twinsToLoad.forEach(twin -> twin.setMarkersLoadState(LoadState.LOADED));
                return;
            }

            Map<UUID, List<TwinEntity>> twinsByClass = twinsWithDynamicMarkers.stream()
                    .collect(Collectors.groupingBy(twin -> twin.getTwinClass().getId()));

            List<TwinClassDynamicMarkerEntity> classDynamicMarkers = twinClassDynamicMarkerService
                    .findByTwinClassIdIn(twinsByClass.keySet());

            if (CollectionUtils.isEmpty(classDynamicMarkers)) {
                twinsToLoad.forEach(twin -> twin.setMarkersLoadState(LoadState.LOADED));
                return;
            }

            Map<UUID, List<TwinClassDynamicMarkerEntity>> dynamicMarkersByClass = classDynamicMarkers.stream()
                    .collect(Collectors.groupingBy(TwinClassDynamicMarkerEntity::getTwinClassId));

            List<UUID> allMarkerOptionIds = classDynamicMarkers.stream()
                    .map(TwinClassDynamicMarkerEntity::getMarkerDataListOptionId)
                    .distinct()
                    .collect(Collectors.toList());

            Kit<DataListOptionEntity, UUID> allMarkerOptions = dataListOptionService.findEntitiesSafe(allMarkerOptionIds);

            for (Map.Entry<UUID, List<TwinClassDynamicMarkerEntity>> classEntry : dynamicMarkersByClass.entrySet()) {
                List<TwinEntity> classTwins = twinsByClass.get(classEntry.getKey());

                if (CollectionUtils.isEmpty(classTwins)) {
                    continue;
                }

                for (TwinClassDynamicMarkerEntity dynamicMarker : classEntry.getValue()) {
                    DataListOptionEntity markerOption = allMarkerOptions.get(dynamicMarker.getMarkerDataListOptionId());
                    if (markerOption == null) {
                        log.warn("Dynamic marker option not found: {}", dynamicMarker.getMarkerDataListOptionId());
                        continue;
                    }

                    Map<UUID, ValidationResult> validationResults = twinValidatorSetService.isValid(classTwins, dynamicMarker);

                    for (TwinEntity twin : classTwins) {
                        ValidationResult result = validationResults.get(twin.getId());
                        if (result != null && result.isValid()) {
                            Kit<DataListOptionEntity, UUID> kit = twin.getTwinMarkerKit();
                            if (kit == null) {
                                kit = new Kit<>(DataListOptionEntity::getId);
                                twin.setTwinMarkerKit(kit);
                            }
                            if (!kit.containsKey(markerOption.getId())) {
                                kit.add(markerOption);
                            }
                        }
                    }
                }
            }

            twinsToLoad.forEach(twin -> twin.setMarkersLoadState(LoadState.LOADED));

        } catch (ServiceException e) {
            for (TwinEntity twin : twinsToLoad) {
                twin.setMarkersLoadState(LoadState.NOT_LOAD);
                twin.setTwinMarkerKit(null);
            }
            throw e;
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
