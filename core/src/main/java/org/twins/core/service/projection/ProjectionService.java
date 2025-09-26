package org.twins.core.service.projection;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.projection.ProjectionEntity;
import org.twins.core.dao.projection.ProjectionRepository;
import org.twins.core.domain.projection.ProjectionCreate;
import org.twins.core.domain.projection.ProjectionUpdate;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.service.twin.TwinPointerService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Lazy
@RequiredArgsConstructor
public class ProjectionService extends EntitySecureFindServiceImpl<ProjectionEntity> {
    private final ProjectionRepository projectionRepository;
    private final TwinPointerService twinPointerService;
    private final FeaturerService featurerService;

    @Override
    public CrudRepository<ProjectionEntity, UUID> entityRepository() {
        return projectionRepository;
    }

    @Override
    public Function<ProjectionEntity, UUID> entityGetIdFunction() {
        return ProjectionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(ProjectionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(ProjectionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getSrcTwinPointerId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty srcTwinPointerId");
        if (entity.getSrcTwinClassFieldId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty srcTwinClassFieldId");
        if (entity.getDstTwinClassFieldId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty dstTwinClassFieldId");
        if (entity.getDstTwinClassId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty dstTwinClassFieldId");
        if (entity.getFieldProjectorFeaturerId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty fieldProjectorFeaturerId");

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getSrcTwinPointer() == null || !entity.getSrcTwinPointer().getId().equals(entity.getSrcTwinPointerId())) {
                    entity.setSrcTwinPointer(twinPointerService.findEntitySafe(entity.getSrcTwinPointerId()));
                }
        }
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<ProjectionEntity> createProjectionList(List<ProjectionCreate> projectionCreates) throws ServiceException {
        if (CollectionUtils.isEmpty(projectionCreates)) {
            return Collections.emptyList();
        }

        List<ProjectionEntity> projectionEntities = new ArrayList<>();

        for (ProjectionCreate projectionCreate : projectionCreates) {
            ProjectionEntity projectionEntity = new ProjectionEntity();
            projectionEntity
                    .setSrcTwinPointerId(projectionCreate.getSrcTwinPointerId())
                    .setSrcTwinClassFieldId(projectionCreate.getSrcTwinClassFieldId())
                    .setDstTwinClassId(projectionCreate.getDstTwinClassId())
                    .setDstTwinClassFieldId(projectionCreate.getDstTwinClassFieldId())
                    .setFieldProjectorFeaturerId(projectionCreate.getFieldProjectorFeaturerId())
                    .setFieldProjectorParams(projectionCreate.getFieldProjectorParams());

            validateEntityAndThrow(projectionEntity, EntitySmartService.EntityValidateMode.beforeSave);
            projectionEntities.add(projectionEntity);
        }
        return StreamSupport.stream(entityRepository().saveAll(projectionEntities).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<ProjectionEntity> updateProjectionList(List<ProjectionUpdate> projectionUpdates) throws ServiceException {
        if (CollectionUtils.isEmpty(projectionUpdates)) {
            return Collections.emptyList();
        }

        Kit<ProjectionEntity, UUID> dbProjectionEntitiesKit = findEntitiesSafe(
                projectionUpdates.stream()
                        .map(ProjectionUpdate::getId)
                        .collect(Collectors.toList())
        );

        ChangesHelperMulti<ProjectionEntity> changes = new ChangesHelperMulti<>();
        List<ProjectionEntity> allEntities = dbProjectionEntitiesKit.getList();

        for (ProjectionUpdate projectionUpdate : projectionUpdates) {
            ProjectionEntity dbProjectionEntity = dbProjectionEntitiesKit.get(projectionUpdate.getId());
            ChangesHelper changesHelper = new ChangesHelper();

            updateEntityFieldByValue(projectionUpdate.getSrcTwinPointerId(), dbProjectionEntity, ProjectionEntity::getSrcTwinPointerId, ProjectionEntity::setSrcTwinPointerId, ProjectionEntity.Fields.srcTwinPointerId, changesHelper);
            updateEntityFieldByValue(projectionUpdate.getSrcTwinClassFieldId(), dbProjectionEntity, ProjectionEntity::getSrcTwinClassFieldId, ProjectionEntity::setSrcTwinClassFieldId, ProjectionEntity.Fields.srcTwinClassFieldId, changesHelper);
            updateEntityFieldByValue(projectionUpdate.getDstTwinClassId(), dbProjectionEntity, ProjectionEntity::getDstTwinClassId, ProjectionEntity::setDstTwinClassId, ProjectionEntity.Fields.dstTwinClassId, changesHelper);
            updateEntityFieldByValue(projectionUpdate.getDstTwinClassFieldId(), dbProjectionEntity, ProjectionEntity::getDstTwinClassFieldId, ProjectionEntity::setDstTwinClassFieldId, ProjectionEntity.Fields.dstTwinClassFieldId, changesHelper);
            updateFieldProjectorFeaturerId(dbProjectionEntity, projectionUpdate.getFieldProjectorFeaturerId(), projectionUpdate.getFieldProjectorParams(), changesHelper);

            changes.add(dbProjectionEntity, changesHelper);
        }
        updateSafe(changes);

        return allEntities;
    }

    public void updateFieldProjectorFeaturerId(ProjectionEntity dbProjectionEntity, Integer newFeaturerId, HashMap<String, String> newFeaturerParams, ChangesHelper changesHelper) throws ServiceException {
        if (newFeaturerId == null || newFeaturerId == 0) {
            if (MapUtils.isEmpty(newFeaturerParams))
                return; //nothing was changed
            else
                newFeaturerId = dbProjectionEntity.getFieldProjectorFeaturerId(); // only params where changed
        }
        if (changesHelper.isChanged(ProjectionEntity.Fields.fieldProjectorFeaturerId, dbProjectionEntity.getFieldProjectorFeaturerId(), newFeaturerId)) {
            featurerService.checkValid(newFeaturerId, newFeaturerParams, FieldTyper.class);
            dbProjectionEntity
                    .setFieldProjectorFeaturerId(newFeaturerId);
        }
        featurerService.prepareForStore(newFeaturerId, newFeaturerParams);
        if (!MapUtils.areEqual(dbProjectionEntity.getFieldProjectorParams(), newFeaturerParams)) {
            changesHelper.add(ProjectionEntity.Fields.fieldProjectorParams, dbProjectionEntity.getFieldProjectorParams(), newFeaturerParams);
            dbProjectionEntity
                    .setFieldProjectorFeaturerId(newFeaturerId);
        }
    }

    public void deleteProjections(Set<UUID> projectionIds) {
        projectionRepository.deleteAllById(projectionIds);
    }
}
