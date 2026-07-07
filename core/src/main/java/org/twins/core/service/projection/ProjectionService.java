package org.twins.core.service.projection;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
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
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class ProjectionService extends EntitySecureFindServiceImpl<ProjectionEntity> {
    private final ProjectionRepository projectionRepository;
    private final TwinPointerService twinPointerService;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;
    @Lazy
    private final TwinClassService twinClassService;
    @Lazy
    private final ProjectionTypeService projectionTypeService;

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
        loadProjectionType(entity);
        return checkDomainAccessDenied(entity.getProjectionType().getDomainId(), entity.logShort(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(ProjectionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getSrcTwinPointerId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty srcTwinPointerId");
        if (entity.getSrcTwinClassFieldId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty srcTwinClassFieldId");
        if (entity.getDstTwinClassFieldId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty dstTwinClassFieldId");
        if (entity.getDstTwinClassId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty dstTwinClassFieldId");
        if (entity.getFieldProjectorFeaturerId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty fieldProjectorFeaturerId");

        switch (entityValidateMode) {
            case beforeSave:
                loadSrcTwinPointer(entity);
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
                    .setFieldProjectorParams(projectionCreate.getFieldProjectorParams())
                    .setProjectionTypeId(projectionCreate.getProjectionTypeId())
                    .setActive(projectionCreate.getActive() != null ? projectionCreate.getActive() : true);

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
            updateEntityFieldByValue(projectionUpdate.getProjectionTypeId(), dbProjectionEntity, ProjectionEntity::getProjectionTypeId, ProjectionEntity::setProjectionTypeId, ProjectionEntity.Fields.projectionTypeId, changesHelper);
            updateEntityFieldByValue(projectionUpdate.getActive(), dbProjectionEntity, ProjectionEntity::getActive, ProjectionEntity::setActive, ProjectionEntity.Fields.active, changesHelper);
            updateFieldProjectorFeaturerId(dbProjectionEntity, projectionUpdate.getFieldProjectorFeaturerId(), projectionUpdate.getFieldProjectorParams(), changesHelper);

            changes.add(dbProjectionEntity, changesHelper);
        }
        updateSafe(changes);

        return allEntities;
    }

    public void updateFieldProjectorFeaturerId(ProjectionEntity dbProjectionEntity, Integer newFeaturerId, HashMap<String, String> newFeaturerParams, ChangesHelper changesHelper) throws ServiceException {
        updateEntityFeaturerField(dbProjectionEntity, newFeaturerId, newFeaturerParams,
                ProjectionEntity::getFieldProjectorFeaturerId, ProjectionEntity::setFieldProjectorFeaturerId,
                ProjectionEntity::getFieldProjectorParams, ProjectionEntity::setFieldProjectorParams,
                ProjectionEntity.Fields.fieldProjectorFeaturerId, ProjectionEntity.Fields.fieldProjectorParams,
                FieldTyper.class, changesHelper);
    }

    public void deleteProjections(Set<UUID> projectionIds) throws ServiceException {
        deleteSafe(projectionIds);
    }

    public void loadSrcTwinPointer(ProjectionEntity src) throws ServiceException {
        loadSrcTwinPointer(Collections.singletonList(src));
    }

    public void loadSrcTwinPointer(Collection<ProjectionEntity> srcCollection) throws ServiceException {
        twinPointerService.load(srcCollection,
                ProjectionEntity::getSrcTwinPointerId,
                ProjectionEntity::getSrcTwinPointer,
                ProjectionEntity::setSrcTwinPointer);
    }

    public void loadSrcTwinClassField(ProjectionEntity src) throws ServiceException {
        loadSrcTwinClassField(Collections.singletonList(src));
    }

    public void loadSrcTwinClassField(Collection<ProjectionEntity> srcCollection) throws ServiceException {
        twinClassFieldService.load(srcCollection,
                ProjectionEntity::getSrcTwinClassFieldId,
                ProjectionEntity::getSrcTwinClassField,
                ProjectionEntity::setSrcTwinClassField);
    }

    public void loadDstTwinClassField(ProjectionEntity src) throws ServiceException {
        loadDstTwinClassField(Collections.singletonList(src));
    }

    public void loadDstTwinClassField(Collection<ProjectionEntity> srcCollection) throws ServiceException {
        twinClassFieldService.load(srcCollection,
                ProjectionEntity::getDstTwinClassFieldId,
                ProjectionEntity::getDstTwinClassField,
                ProjectionEntity::setDstTwinClassField);
    }

    public void loadDstTwinClass(ProjectionEntity src) throws ServiceException {
        loadDstTwinClass(Collections.singletonList(src));
    }

    public void loadDstTwinClass(Collection<ProjectionEntity> srcCollection) throws ServiceException {
        twinClassService.load(srcCollection,
                ProjectionEntity::getDstTwinClassId,
                ProjectionEntity::getDstTwinClass,
                ProjectionEntity::setDstTwinClass);
    }

    public void loadProjectionType(ProjectionEntity src) throws ServiceException {
        loadProjectionType(Collections.singletonList(src));
    }

    public void loadProjectionType(Collection<ProjectionEntity> srcCollection) throws ServiceException {
        projectionTypeService.load(srcCollection,
                ProjectionEntity::getProjectionTypeId,
                ProjectionEntity::getProjectionType,
                ProjectionEntity::setProjectionType);
    }

}
