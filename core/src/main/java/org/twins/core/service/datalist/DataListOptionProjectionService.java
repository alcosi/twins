package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.KitUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionProjectionEntity;
import org.twins.core.dao.datalist.DataListOptionProjectionRepository;
import org.twins.core.dao.datalist.DataListProjectionEntity;

import java.util.*;
import java.util.function.Function;

@Service
@Lazy
@RequiredArgsConstructor
public class DataListOptionProjectionService extends EntitySecureFindServiceImpl<DataListOptionProjectionEntity> {
    private final DataListOptionProjectionRepository dataListOptionProjectionRepository;

    @Lazy
    @Autowired
    private DataListOptionService dataListOptionService;

    @Lazy
    @Autowired
    private DataListProjectionService dataListProjectionService;

    @Override
    public CrudRepository<DataListOptionProjectionEntity, UUID> entityRepository() {
        return dataListOptionProjectionRepository;
    }

    @Override
    public Function<DataListOptionProjectionEntity, UUID> entityGetIdFunction() {
        return DataListOptionProjectionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DataListOptionProjectionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(DataListOptionProjectionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadDataListOptions(DataListOptionProjectionEntity src) throws ServiceException {
        loadDataListOptions(Collections.singletonList(src));
    }

    public void loadDataListOptions(Collection<DataListOptionProjectionEntity> projections) throws ServiceException {
        if (projections == null || projections.isEmpty())
            return;
        Set<UUID> needIds = new HashSet<>();
        for (DataListOptionProjectionEntity p : projections) {
            if (p.getSrcDataListOption() == null && p.getSrcDataListOptionId() != null)
                needIds.add(p.getSrcDataListOptionId());
            if (p.getDstDataListOption() == null && p.getDstDataListOptionId() != null)
                needIds.add(p.getDstDataListOptionId());
        }
        if (needIds.isEmpty())
            return;
        Kit<DataListOptionEntity, UUID> items = dataListOptionService.findEntitiesSafe(needIds);
        for (DataListOptionProjectionEntity p : projections) {
            if (p.getSrcDataListOption() == null && p.getSrcDataListOptionId() != null)
                p.setSrcDataListOption(items.get(p.getSrcDataListOptionId()));
            if (p.getDstDataListOption() == null && p.getDstDataListOptionId() != null)
                p.setDstDataListOption(items.get(p.getDstDataListOptionId()));
        }
    }

    public void loadDataListProjections(DataListOptionProjectionEntity src) throws ServiceException {
        loadDataListProjections(Collections.singletonList(src));
    }

    public void loadDataListProjections(Collection<DataListOptionProjectionEntity> projections) throws ServiceException {
        if (projections == null || projections.isEmpty())
            return;
        KitGrouped<DataListOptionProjectionEntity, UUID, UUID> needLoad = new KitGrouped<>(DataListOptionProjectionEntity::getId, DataListOptionProjectionEntity::getDataListProjectionId);
        for (DataListOptionProjectionEntity p : projections) {
            if (p.getDataListProjectionId() != null && p.getDataListProjection() == null)
                needLoad.add(p);
        }
        if (KitUtils.isEmpty(needLoad))
            return;
        Kit<DataListProjectionEntity, UUID> items = dataListProjectionService.findEntitiesSafe(needLoad.getGroupedKeySet());
        for (var p : needLoad)
            p.setDataListProjection(items.get(p.getDataListProjectionId()));
    }
}
