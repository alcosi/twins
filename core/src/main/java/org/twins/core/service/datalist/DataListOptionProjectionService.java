package org.twins.core.service.datalist;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
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
import org.twins.core.dao.datalist.DataListOptionProjectionEntity;
import org.twins.core.dao.datalist.DataListOptionProjectionRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.projection.ProjectionTypeService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
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
    private ProjectionTypeService projectionTypeService;

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
        if (entity.getProjectionTypeId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty projectionTypeId");
        if (entity.getSrcDataListOptionId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty srcDataListOptionId");
        if (entity.getDstDataListOptionId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty dstDataListOptionId");

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getProjectionType() == null || !entity.getProjectionType().getId().equals(entity.getProjectionTypeId())) {
                    entity.setProjectionType(projectionTypeService.findEntitySafe(entity.getProjectionTypeId()));
                }
                if (entity.getSrcDataListOption() == null || entity.getDstDataListOption() == null || !entity.getSrcDataListOption().getId().equals(entity.getSrcDataListOptionId()) || !entity.getDstDataListOption().getId().equals(entity.getDstDataListOptionId())) {
                    loadDataListOptions(entity);
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
        dataListOptionService.load(projections,
                new LoadedField<>(
                        DataListOptionProjectionEntity::getSrcDataListOptionId,
                        DataListOptionProjectionEntity::getSrcDataListOption,
                        DataListOptionProjectionEntity::setSrcDataListOption),
                new LoadedField<>(
                        DataListOptionProjectionEntity::getDstDataListOptionId,
                        DataListOptionProjectionEntity::getDstDataListOption,
                        DataListOptionProjectionEntity::setDstDataListOption));
    }

    public void loadProjectionTypes(DataListOptionProjectionEntity src) throws ServiceException {
        loadProjectionTypes(Collections.singletonList(src));
    }

    public void loadProjectionTypes(Collection<DataListOptionProjectionEntity> projections) throws ServiceException {
        projectionTypeService.load(projections,
                DataListOptionProjectionEntity::getProjectionTypeId,
                DataListOptionProjectionEntity::getProjectionType,
                DataListOptionProjectionEntity::setProjectionType);
    }

    public void loadSavedByUser(DataListOptionProjectionEntity src) throws ServiceException {
        loadSavedByUser(Collections.singletonList(src));
    }

    public void loadSavedByUser(Collection<DataListOptionProjectionEntity> projections) throws ServiceException {
        userService.load(projections,
                DataListOptionProjectionEntity::getSavedByUserId,
                DataListOptionProjectionEntity::getSavedByUser,
                DataListOptionProjectionEntity::setSavedByUser);
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
        }
        return StreamSupport.stream(saveSafe(dataListOptionProjectionEntities).spliterator(), false).toList();
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

            updateEntityFieldByEntity(entity, dbEntity, DataListOptionProjectionEntity::getProjectionTypeId, DataListOptionProjectionEntity::setProjectionTypeId, DataListOptionProjectionEntity.Fields.projectionTypeId, changesHelper);
            updateEntityFieldByEntity(entity, dbEntity, DataListOptionProjectionEntity::getSrcDataListOptionId, DataListOptionProjectionEntity::setSrcDataListOptionId, DataListOptionProjectionEntity.Fields.srcDataListOptionId, changesHelper);
            updateEntityFieldByEntity(entity, dbEntity, DataListOptionProjectionEntity::getDstDataListOptionId, DataListOptionProjectionEntity::setDstDataListOptionId, DataListOptionProjectionEntity.Fields.dstDataListOptionId, changesHelper);
            updateEntityFieldByValue(Timestamp.valueOf(LocalDateTime.now()), dbEntity, DataListOptionProjectionEntity::getChangedAt, DataListOptionProjectionEntity::setChangedAt, DataListOptionProjectionEntity.Fields.changedAt, changesHelper);
            changes.add(dbEntity, changesHelper);
        }

        updateSafe(changes);

        return allEntities;
    }
}
