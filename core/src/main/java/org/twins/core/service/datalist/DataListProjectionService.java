package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
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
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListProjectionEntity;
import org.twins.core.dao.datalist.DataListProjectionRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Service
@Lazy
@RequiredArgsConstructor
public class DataListProjectionService extends EntitySecureFindServiceImpl<DataListProjectionEntity> {
    private final DataListProjectionRepository dataListProjectionRepository;
    private final AuthService authService;
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

    @Transactional(rollbackFor = Throwable.class)
    public List<DataListProjectionEntity> createDataListProjections(List<DataListProjectionEntity> dataListProjectionEntities) throws ServiceException {
        if (CollectionUtils.isEmpty(dataListProjectionEntities)) {
            return Collections.emptyList();
        }

        ApiUser apiUser = authService.getApiUser();

        for (DataListProjectionEntity entity : dataListProjectionEntities) {
            entity
                    .setSavedByUserId(apiUser.getUserId())
                    .setSavedByUser(apiUser.getUser())
                    .setChangedAt(Timestamp.valueOf(LocalDateTime.now()));
            validateEntityAndThrow(entity, EntitySmartService.EntityValidateMode.beforeSave);
        }

        return StreamSupport.stream(entityRepository().saveAll(dataListProjectionEntities).spliterator(), false).toList();
    }

    public List<DataListProjectionEntity> updateDataListProjections(List<DataListProjectionEntity> dataListProjectionEntities) throws ServiceException {
        if (CollectionUtils.isEmpty(dataListProjectionEntities)) {
            return Collections.emptyList();
        }

        Kit<DataListProjectionEntity, UUID> dbDataListProjectionEntitiesKit = findEntitiesSafe(dataListProjectionEntities.stream().map(DataListProjectionEntity::getId).toList());

        ChangesHelperMulti<DataListProjectionEntity> changes = new ChangesHelperMulti<>();
        List<DataListProjectionEntity> allEntities = dbDataListProjectionEntitiesKit.getList();

        for (DataListProjectionEntity entity : dataListProjectionEntities) {
            DataListProjectionEntity dbEntity = dbDataListProjectionEntitiesKit.get(entity.getId());
            ChangesHelper changesHelper = new ChangesHelper();

           updateEntityFieldByEntity(entity, dbEntity, DataListProjectionEntity::getName, DataListProjectionEntity::setName, DataListProjectionEntity.Fields.name, changesHelper);
           updateEntityFieldByEntity(entity, dbEntity, DataListProjectionEntity::getSrcDataListId, DataListProjectionEntity::setSrcDataListId, DataListProjectionEntity.Fields.srcDataListId, changesHelper);
           updateEntityFieldByEntity(entity, dbEntity, DataListProjectionEntity::getDstDataListId, DataListProjectionEntity::setDstDataListId, DataListProjectionEntity.Fields.dstDataListId, changesHelper);
           updateEntityFieldByValue(Timestamp.valueOf(LocalDateTime.now()), dbEntity, DataListProjectionEntity::getChangedAt, DataListProjectionEntity::setChangedAt, DataListProjectionEntity.Fields.changedAt, changesHelper);
           changes.add(dbEntity, changesHelper);
        }
        updateSafe(changes);

        return allEntities;
    }

}
