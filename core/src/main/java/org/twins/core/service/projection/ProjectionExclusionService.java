package org.twins.core.service.projection;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.projection.ProjectionExclusionEntity;
import org.twins.core.dao.projection.ProjectionExclusionRepository;
import org.twins.core.domain.projection.ProjectionExclusionCreate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Service
@Lazy
@RequiredArgsConstructor
public class ProjectionExclusionService extends EntitySecureFindServiceImpl<ProjectionExclusionEntity> {
    private final ProjectionExclusionRepository projectionExclusionRepository;


    @Override
    public CrudRepository<ProjectionExclusionEntity, UUID> entityRepository() {
        return projectionExclusionRepository;
    }

    @Override
    public Function<ProjectionExclusionEntity, UUID> entityGetIdFunction() {
        return ProjectionExclusionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(ProjectionExclusionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(ProjectionExclusionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinId");
        if (entity.getTwinClassFieldId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinClassFieldId");

        return true;
    }

    public List<ProjectionExclusionEntity> createProjectionExclusionList(List<ProjectionExclusionCreate> projectionExclusionCreates) throws ServiceException {
        if (CollectionUtils.isEmpty(projectionExclusionCreates)) {
            return Collections.emptyList();
        }

        List<ProjectionExclusionEntity> projectionExclusionEntities = new ArrayList<>();

        for (ProjectionExclusionCreate projectionExclusionCreate : projectionExclusionCreates) {
            ProjectionExclusionEntity projectionExclusionEntity = new ProjectionExclusionEntity();
            projectionExclusionEntity
                    .setTwinId(projectionExclusionCreate.getTwinId())
                    .setTwinClassFieldId(projectionExclusionCreate.getTwinClassFieldId());

            validateEntityAndThrow(projectionExclusionEntity, EntitySmartService.EntityValidateMode.beforeSave);
            projectionExclusionEntities.add(projectionExclusionEntity);
        }

        return StreamSupport.stream(entityRepository().saveAll(projectionExclusionEntities).spliterator(), false).toList();
    }

    public void deleteProjectionExclusions(Set<UUID> projectionIds) {
        entityRepository().deleteAllById(projectionIds);
    }
}
