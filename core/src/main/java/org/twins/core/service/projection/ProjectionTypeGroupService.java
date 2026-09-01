package org.twins.core.service.projection;

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
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;
import org.twins.core.dao.projection.ProjectionTypeGroupRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class ProjectionTypeGroupService extends EntitySecureFindServiceImpl<ProjectionTypeGroupEntity> {
    private final ProjectionTypeGroupRepository projectionTypeGroupRepository;
    private final AuthService authService;

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
        return checkDomainAccessDenied(entity.getDomainId(), "projectionTypeGroup[" + entity.getId() + "]", readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(ProjectionTypeGroupEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<ProjectionTypeGroupEntity> createProjectionTypeGroups(List<ProjectionTypeGroupEntity> projectionTypeGroupEntities) throws ServiceException {
        if (CollectionUtils.isEmpty(projectionTypeGroupEntities)) {
            return Collections.emptyList();
        }
        ApiUser apiUser = authService.getApiUser();
        for (ProjectionTypeGroupEntity entity : projectionTypeGroupEntities) {
            entity.setDomainId(apiUser.getDomainId());
        }
        return StreamSupport.stream(saveSafe(projectionTypeGroupEntities).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<ProjectionTypeGroupEntity> updateProjectionTypeGroups(List<ProjectionTypeGroupEntity> projectionTypeGroupEntities) throws ServiceException {
        if (CollectionUtils.isEmpty(projectionTypeGroupEntities)) {
            return Collections.emptyList();
        }
        Kit<ProjectionTypeGroupEntity, UUID> dbEntitiesKit = findEntitiesSafe(projectionTypeGroupEntities.stream().map(ProjectionTypeGroupEntity::getId).toList());
        ChangesHelperMulti<ProjectionTypeGroupEntity> changes = new ChangesHelperMulti<>();
        List<ProjectionTypeGroupEntity> allEntities = new ArrayList<>(projectionTypeGroupEntities.size());
        for (ProjectionTypeGroupEntity entity : projectionTypeGroupEntities) {
            ProjectionTypeGroupEntity dbEntity = dbEntitiesKit.get(entity.getId());
            allEntities.add(dbEntity);
            ChangesHelper changesHelper = new ChangesHelper();
            updateEntityFieldByEntity(entity, dbEntity, ProjectionTypeGroupEntity::getKey, ProjectionTypeGroupEntity::setKey, ProjectionTypeGroupEntity.Fields.key, changesHelper);
            if (changesHelper.hasChanges()) {
                changes.add(dbEntity, changesHelper);
            }
        }
        updateSafe(changes);
        return allEntities;
    }
}
