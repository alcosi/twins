package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.KitUtils;
import org.cambium.common.util.UuidUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinMarkerEntity;
import org.twins.core.dao.twin.TwinMarkerRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ReplaceOperation;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;
import java.util.stream.Collectors;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinMarkerService extends EntitySecureFindServiceImpl<TwinMarkerEntity> {
    final AuthService authService;
    final TwinMarkerRepository twinMarkerRepository;
    final TwinService twinService;
    final DataListService dataListService;
    final EntitySmartService entitySmartService;
    @Lazy
    final TwinClassService twinClassService;

    @Override
    public CrudRepository<TwinMarkerEntity, UUID> entityRepository() {
        return twinMarkerRepository;
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
                if (!entity.getTwin().getTwinClass().getMarkerDataListId().equals(entity.getMarkerDataListOption().getDataListId()))
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

    public Kit<DataListOptionEntity, UUID> loadMarkers(TwinEntity twinEntity) {
        if (twinEntity.getTwinMarkerKit() != null)
            return twinEntity.getTwinMarkerKit();
        List<DataListOptionEntity> dataListOptionEntityList = findDataListOptionByTwinId(twinEntity.getId());
        if (dataListOptionEntityList != null)
            twinEntity.setTwinMarkerKit(new Kit<>(dataListOptionEntityList, DataListOptionEntity::getId));
        return twinEntity.getTwinMarkerKit();
    }

    public void loadMarkers(Collection<TwinEntity> twinEntityList) {
        Map<UUID, TwinEntity> needLoad = new HashMap<>();
        for (TwinEntity twinEntity : twinEntityList)
            if (twinEntity.getTwinMarkerKit() == null)
                needLoad.put(twinEntity.getId(), twinEntity);
        if (needLoad.isEmpty())
            return;
        List<TwinMarkerEntity> twinMarkerEntityList = twinMarkerRepository.findByTwinIdIn(needLoad.keySet());
        if (CollectionUtils.isEmpty(twinMarkerEntityList))
            return;
        Map<UUID, List<DataListOptionEntity>> markersMap = new HashMap<>(); // key - twinId
        for (TwinMarkerEntity twinMarkerEntity : twinMarkerEntityList) { //grouping by twin
            markersMap.computeIfAbsent(twinMarkerEntity.getTwinId(), k -> new ArrayList<>());
            markersMap.get(twinMarkerEntity.getTwinId()).add(twinMarkerEntity.getMarkerDataListOption());
        }
        TwinEntity twinEntity;
        List<DataListOptionEntity> twinMarkers;
        for (Map.Entry<UUID, TwinEntity> entry : needLoad.entrySet()) {
            twinEntity = entry.getValue();
            twinMarkers = markersMap.get(entry.getKey());
            twinEntity.setTwinMarkerKit(new Kit<>(twinMarkers, DataListOptionEntity::getId));
        }
    }

    public boolean hasMarker(TwinEntity twinEntity, UUID marker) {
        Kit<DataListOptionEntity, UUID> markers = loadMarkers(twinEntity);
        return KitUtils.isNotEmpty(markers) && markers.getIdSet().contains(marker);
    }

    public void addMarkers(TwinEntity twinEntity, Set<UUID> markersAdd) throws ServiceException {
        if (CollectionUtils.isEmpty(markersAdd))
            return;

        List<TwinMarkerEntity> existingMarkers = twinMarkerRepository.findByTwinId(twinEntity.getId());
        Set<UUID> existingMarkerIds = existingMarkers.stream()
                .map(TwinMarkerEntity::getMarkerDataListOptionId)
                .collect(Collectors.toSet());

        List<TwinMarkerEntity> list = new ArrayList<>();
        for (UUID marker : markersAdd) {
            if (!existingMarkerIds.contains(marker)) {
                TwinMarkerEntity twinMarkerEntity = new TwinMarkerEntity()
                        .setTwinId(twinEntity.getId())
                        .setTwin(twinEntity)
                        .setMarkerDataListOptionId(marker);
                validateEntityAndThrow(twinMarkerEntity, EntitySmartService.EntityValidateMode.beforeSave);
                list.add(twinMarkerEntity);
            }
        }
        entitySmartService.saveAllAndLog(list, twinMarkerRepository);
        twinEntity.setTwinMarkerKit(null); // invalidating already loaded kit
    }

    public void deleteMarkers(TwinEntity twinEntity, Set<UUID> markersDelete) throws ServiceException {
        if (CollectionUtils.isEmpty(markersDelete))
            return;
        twinMarkerRepository.deleteByTwinIdAndMarkerDataListOptionIdIn(twinEntity.getId(), markersDelete);
        log.info("Markers[" + StringUtils.join(markersDelete, ",") + "] perhaps were deleted from " + twinEntity.logShort());
        twinEntity.setTwinMarkerKit(null); // invalidating already loaded kit
    }

    @Transactional
    public void deleteAllMarkersForTwinsOfClass(UUID twinClassId) {
        twinMarkerRepository.deleteByTwin_TwinClassId(twinClassId);
    }

    public Set<UUID> findExistedTwinMarkersForTwinsOfClass(UUID twinClassId) {
        return twinMarkerRepository.findDistinctMakersDataListOptionIdByTwinTwinClassId(twinClassId);
    }

    @Transactional
    public void replaceMarkersForTwinsOfClass(TwinClassEntity twinClassEntity, ReplaceOperation replaceOperation) throws ServiceException {
        Set<UUID> existedTwinMarkerIds = findExistedTwinMarkersForTwinsOfClass(twinClassEntity.getId());
        if (CollectionUtils.isNotEmpty(existedTwinMarkerIds)) {
            if (replaceOperation.getStrategy() == ReplaceOperation.Strategy.restrictIfMissed
                    && MapUtils.isEmpty(replaceOperation.getReplaceMap()))
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide markersReplaceMap for markers: " + org.cambium.common.util.StringUtils.join(existedTwinMarkerIds));
            twinClassService.loadMarkerDataList(twinClassEntity);
            Set<UUID> markersForDeletion = new HashSet<>();
            for (UUID markerForReplace : existedTwinMarkerIds) {
                UUID replacement = replaceOperation.getReplaceMap().get(markerForReplace);
                if (replacement == null) {
                    if (replaceOperation.getStrategy() == ReplaceOperation.Strategy.restrictIfMissed)
                        throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide markersReplaceMap value for marker: " + markerForReplace);
                    else
                        replacement = UuidUtils.NULLIFY_MARKER;
                }
                if (UuidUtils.isNullifyMarker(replacement)) {
                    markersForDeletion.add(markerForReplace);
                    continue;
                }
                if (twinClassEntity.getMarkerDataList().getOptions().get(replacement) == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide correct markersReplaceMap value for marker: " + markerForReplace);
                twinMarkerRepository.replaceMarkersForTwinsOfClass(twinClassEntity.getId(), markerForReplace, replacement);
            }
            if (CollectionUtils.isNotEmpty(markersForDeletion)) {
                twinMarkerRepository.deleteByTwin_TwinClassIdAndMarkerDataListOptionIdIn(twinClassEntity.getId(), markersForDeletion);
            }
        }
    }
}
