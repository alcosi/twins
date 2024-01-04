package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.Kit;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinMarkerEntity;
import org.twins.core.dao.twin.TwinMarkerRepository;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.datalist.DataListService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinMarkerService extends EntitySecureFindServiceImpl<TwinMarkerEntity> {
    final TwinMarkerRepository twinMarkerRepository;
    final TwinService twinService;
    final DataListService dataListService;
    final EntitySmartService entitySmartService;

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

    public Kit<DataListOptionEntity> loadMarkers(TwinEntity twinEntity) {
        if (twinEntity.getTwinMarkerKit() != null)
            return twinEntity.getTwinMarkerKit();
        List<DataListOptionEntity> dataListOptionEntityList = findDataListOptionByTwinId(twinEntity.getId());
        if (dataListOptionEntityList != null)
            twinEntity.setTwinMarkerKit(new Kit<>(dataListOptionEntityList, DataListOptionEntity::getId));
        return twinEntity.getTwinMarkerKit();
    }

    public boolean hasMarker(TwinEntity twinEntity, UUID marker) {
        Kit<DataListOptionEntity> markers = loadMarkers(twinEntity);
        return markers != null && markers.getIdSet().contains(marker);
    }

    public void addMarkers(TwinEntity twinEntity, Set<UUID> markersAdd) throws ServiceException {
        if (CollectionUtils.isEmpty(markersAdd))
            return;
        List<TwinMarkerEntity> list = new ArrayList<>();
        TwinMarkerEntity twinMarkerEntity;
        for (UUID marker : markersAdd) {
            twinMarkerEntity = new TwinMarkerEntity()
                    .setTwinId(twinEntity.getId())
                    .setTwin(twinEntity)
                    .setMarkerDataListOptionId(marker);
            validateEntityAndThrow(twinMarkerEntity, EntitySmartService.EntityValidateMode.beforeSave);
            list.add(twinMarkerEntity);
        }
        entitySmartService.saveAllAndLog(list, twinMarkerRepository);
        twinEntity.setTwinMarkerKit(null); // invalidating already loaded kit
    }

    public void deleteMarkers(TwinEntity twinEntity, Set<UUID> markersDelete) throws ServiceException {
        if (CollectionUtils.isEmpty(markersDelete))
            return;
        entitySmartService.deleteAllAndLog(markersDelete, twinMarkerRepository);
        twinEntity.setTwinMarkerKit(null); // invalidating already loaded kit
    }
}
