package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.KitUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionProjectionEntity;
import org.twins.core.dao.datalist.DataListOptionProjectionRepository;
import org.twins.core.dao.datalist.DataListProjectionEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Service
@Lazy
@RequiredArgsConstructor
public class DataListOptionProjectionService extends EntitySecureFindServiceImpl<DataListOptionProjectionEntity> {
    private final DataListOptionProjectionRepository dataListOptionProjectionRepository;
    private final AuthService authService;
    @Lazy
    private final UserService userService;

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
        if (entity.getDataListProjectionId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty dataListProjectionId");
        if (entity.getSrcDataListOptionId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty srcDataListOptionId");
        if (entity.getDstDataListOptionId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty dstDataListOptionId");

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getDataListProjection() == null || !entity.getDataListProjection().getId().equals(entity.getDataListProjectionId())) {
                    entity.setDataListProjection(dataListProjectionService.findEntitySafe(entity.getDataListProjectionId()));
                }
                if (entity.getSrcDataListOption() == null || !entity.getSrcDataListOption().getId().equals(entity.getSrcDataListOptionId())) {
                    entity.setSrcDataListOption(dataListOptionService.findEntitySafe(entity.getSrcDataListOptionId()));
                }
                if (entity.getDstDataListOption() == null || !entity.getDstDataListOption().getId().equals(entity.getDstDataListOptionId())) {
                    entity.setDstDataListOption(dataListOptionService.findEntitySafe(entity.getDstDataListOptionId()));
                }
                if (entity.getSavedByUser() == null) {
                    entity.setSavedByUser(userService.findEntitySafe(entity.getSavedByUserId()));
                }
        }

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

    @Transactional(rollbackFor = Throwable.class)
    public List<DataListOptionProjectionEntity> createDataListOptionProjections(List<DataListOptionProjectionEntity> dataListOptionProjectionEntities) throws ServiceException {
        if (CollectionUtils.isEmpty(dataListOptionProjectionEntities)) {
            return Collections.emptyList();
        }

        ApiUser apiUser = authService.getApiUser();

        for (DataListOptionProjectionEntity entity : dataListOptionProjectionEntities) {
            entity
                    .setSavedByUser(apiUser.getUser())
                    .setSavedByUserId(apiUser.getUser().getId())
                    .setChangedAt(Timestamp.valueOf(LocalDateTime.now()));

            validateEntityAndThrow(entity, EntitySmartService.EntityValidateMode.beforeSave);
        }
        return StreamSupport.stream(entityRepository().saveAll(dataListOptionProjectionEntities).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<DataListOptionProjectionEntity> updateDataListOptionProjections(List<DataListOptionProjectionEntity> dataListOptionProjectionEntities) throws ServiceException {
        if (CollectionUtils.isEmpty(dataListOptionProjectionEntities)) {
            return Collections.emptyList();
        }

        Kit<DataListOptionProjectionEntity, UUID> dbDataListOptionProjectionEntitiesKit = findEntitiesSafe(dataListOptionProjectionEntities.stream().map(DataListOptionProjectionEntity::getId).toList());

        ChangesHelperMulti<DataListOptionProjectionEntity> changes = new ChangesHelperMulti<>();
        List<DataListOptionProjectionEntity> allEntities = dbDataListOptionProjectionEntitiesKit.getList();

        for (DataListOptionProjectionEntity entity : allEntities) {
            DataListOptionProjectionEntity dbEntity = dbDataListOptionProjectionEntitiesKit.get(entity.getId());
            ChangesHelper changesHelper = new ChangesHelper();

            updateEntityFieldByEntity(entity, dbEntity, DataListOptionProjectionEntity::getDataListProjectionId, DataListOptionProjectionEntity::setDataListProjectionId, DataListOptionProjectionEntity.Fields.dataListProjectionId, changesHelper);
            updateEntityFieldByEntity(entity, dbEntity, DataListOptionProjectionEntity::getSrcDataListOptionId, DataListOptionProjectionEntity::setSrcDataListOptionId, DataListOptionProjectionEntity.Fields.srcDataListOptionId, changesHelper);
            updateEntityFieldByEntity(entity, dbEntity, DataListOptionProjectionEntity::getDstDataListOptionId, DataListOptionProjectionEntity::setDstDataListOptionId, DataListOptionProjectionEntity.Fields.dstDataListOptionId, changesHelper);
            updateEntityFieldByValue(Timestamp.valueOf(LocalDateTime.now()), dbEntity, DataListOptionProjectionEntity::getChangedAt, DataListOptionProjectionEntity::setChangedAt, DataListOptionProjectionEntity.Fields.changedAt, changesHelper);
            changes.add(dbEntity, changesHelper);
        }

        updateSafe(changes);

        return allEntities;
    }
}
