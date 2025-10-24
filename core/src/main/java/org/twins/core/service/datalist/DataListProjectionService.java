package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListProjectionEntity;
import org.twins.core.dao.datalist.DataListProjectionRepository;

import java.util.*;
import java.util.function.Function;

@Service
@Lazy
@RequiredArgsConstructor
public class DataListProjectionService extends EntitySecureFindServiceImpl<DataListProjectionEntity> {
    private final DataListProjectionRepository dataListProjectionRepository;

    @Lazy
    @Autowired
    private DataListService dataListService;

    @Override
    public CrudRepository<DataListProjectionEntity, UUID> entityRepository() {
        return dataListProjectionRepository;
    }

    @Override
    public Function<DataListProjectionEntity, UUID> entityGetIdFunction() {
        return DataListProjectionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DataListProjectionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(DataListProjectionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getSrcDataListId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty srcDataListId");
        if (entity.getDstDataListId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty dstDataListId");

        switch (entityValidateMode) {
            case beforeSave:
                loadDataList(entity); //todo move to beforeValidateEntities
                UUID srcDomain = entity.getSrcDataList().getDomainId();
                UUID dstDomain = entity.getDstDataList().getDomainId();
                if (srcDomain != null && dstDomain != null && !srcDomain.equals(dstDomain)) {
                    return logErrorAndReturnFalse(entity.logNormal() + " src/dst dataLists belong to different domains: " + srcDomain + " vs " + dstDomain);
                }
                break;
        }
        return true;
    }

    public void loadDataList(DataListProjectionEntity src) throws ServiceException {
        loadDataLists(Collections.singletonList(src));
    }

    public void loadDataLists(Collection<DataListProjectionEntity> projections) throws ServiceException {
        if (projections == null || projections.isEmpty())
            return;
        Set<UUID> needIds = new HashSet<>();
        for (DataListProjectionEntity p : projections) {
            if (p.getSrcDataList() == null && p.getSrcDataListId() != null)
                needIds.add(p.getSrcDataListId());
            if (p.getDstDataList() == null && p.getDstDataListId() != null)
                needIds.add(p.getDstDataListId());
        }
        if (needIds.isEmpty())
            return;
        Kit<DataListEntity, UUID> items = dataListService.findEntitiesSafe(needIds);
        for (DataListProjectionEntity p : projections) {
            if (p.getSrcDataList() == null && p.getSrcDataListId() != null)
                p.setSrcDataList(items.get(p.getSrcDataListId()));
            if (p.getDstDataList() == null && p.getDstDataListId() != null)
                p.setDstDataList(items.get(p.getDstDataListId()));
        }
    }
}
