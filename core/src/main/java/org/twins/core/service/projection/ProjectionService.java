package org.twins.core.service.projection;

import lombok.RequiredArgsConstructor;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.projection.ProjectionEntity;
import org.twins.core.dao.projection.ProjectionRepository;
import org.twins.core.domain.projection.ProjectionCreate;
import org.twins.core.service.twin.TwinPointerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Service
@Lazy
@RequiredArgsConstructor
public class ProjectionService extends EntitySecureFindServiceImpl<ProjectionEntity> {
    private final ProjectionRepository projectionRepository;
    private final TwinPointerService twinPointerService;

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

    public void deleteProjections(Set<UUID> projectionIds) {
        projectionRepository.deleteAllById(projectionIds);
    }
}
