package org.twins.core.service.projection;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;
import org.twins.core.dao.projection.ProjectionTypeGroupRepository;

import java.util.UUID;
import java.util.function.Function;

@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class ProjectionTypeGroupService extends EntitySecureFindServiceImpl<ProjectionTypeGroupEntity> {
    private final ProjectionTypeGroupRepository projectionTypeGroupRepository;

    @Override
    public CrudRepository<ProjectionTypeGroupEntity, UUID> entityRepository() {
        return projectionTypeGroupRepository;
    }

    @Override
    public Function<ProjectionTypeGroupEntity, UUID> entityGetIdFunction() {
        return ProjectionTypeGroupEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(ProjectionTypeGroupEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }


    @Override
    public boolean validateEntity(ProjectionTypeGroupEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
