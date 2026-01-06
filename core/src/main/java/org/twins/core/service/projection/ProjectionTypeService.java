package org.twins.core.service.projection;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dao.projection.ProjectionTypeRepository;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class ProjectionTypeService extends EntitySecureFindServiceImpl<ProjectionTypeEntity> {
    private final ProjectionTypeRepository projectionTypeRepository;
    private final AuthService authService;
    @Lazy
    private final ProjectionTypeGroupService projectionTypeGroupService;
    @Lazy
    private final TwinClassService twinClassService;

    @Override
    public CrudRepository<ProjectionTypeEntity, UUID> entityRepository() {
        return projectionTypeRepository;
    }

    @Override
    public Function<ProjectionTypeEntity, UUID> entityGetIdFunction() {
        return ProjectionTypeEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(ProjectionTypeEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(ProjectionTypeEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getDomainId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty domainId");
        if (entity.getKey() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty key");
        if (entity.getProjectionTypeGroupId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty projectionTypeGroupId");
        if (entity.getMembershipTwinClassId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty membershipTwinClassId");

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getProjectionTypeGroup() == null || !entity.getProjectionTypeGroup().getId().equals(entity.getProjectionTypeGroupId())) {
                    entity.setProjectionTypeGroup(projectionTypeGroupService.findEntitySafe(entity.getProjectionTypeGroupId()));
                }
                if (entity.getMembershipTwinClass() == null || !entity.getMembershipTwinClass().getId().equals(entity.getMembershipTwinClassId())) {
                    entity.setMembershipTwinClass(twinClassService.findEntitySafe(entity.getMembershipTwinClassId()));
                }
        }
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<ProjectionTypeEntity> createProjectionTypes(List<ProjectionTypeEntity> ProjectionTypeEntities) throws ServiceException {
        if (CollectionUtils.isEmpty(ProjectionTypeEntities)) {
            return Collections.emptyList();
        }

        UUID domainId = authService.getApiUser().getDomainId();

        for (ProjectionTypeEntity entity : ProjectionTypeEntities) {
            entity
                    .setDomainId(domainId);
        }

        return StreamSupport.stream(saveSafe(ProjectionTypeEntities).spliterator(), false).toList();
    }

    public List<ProjectionTypeEntity> updateProjectionTypes(List<ProjectionTypeEntity> ProjectionTypeEntities) throws ServiceException {
        if (CollectionUtils.isEmpty(ProjectionTypeEntities)) {
            return Collections.emptyList();
        }

        Kit<ProjectionTypeEntity, UUID> dbProjectionTypeEntitiesKit = findEntitiesSafe(ProjectionTypeEntities.stream().map(ProjectionTypeEntity::getId).toList());

        ChangesHelperMulti<ProjectionTypeEntity> changes = new ChangesHelperMulti<>();
        List<ProjectionTypeEntity> allEntities = dbProjectionTypeEntitiesKit.getList();

        for (ProjectionTypeEntity entity : ProjectionTypeEntities) {
            ProjectionTypeEntity dbEntity = dbProjectionTypeEntitiesKit.get(entity.getId());
            ChangesHelper changesHelper = new ChangesHelper();

            updateEntityFieldByEntity(entity, dbEntity, ProjectionTypeEntity::getName, ProjectionTypeEntity::setName, ProjectionTypeEntity.Fields.name, changesHelper);
            updateEntityFieldByEntity(entity, dbEntity, ProjectionTypeEntity::getKey, ProjectionTypeEntity::setKey, ProjectionTypeEntity.Fields.key, changesHelper);
            updateEntityFieldByEntity(entity, dbEntity, ProjectionTypeEntity::getProjectionTypeGroupId, ProjectionTypeEntity::setProjectionTypeGroupId, ProjectionTypeEntity.Fields.projectionTypeGroupId, changesHelper);
            updateEntityFieldByValue(Timestamp.valueOf(LocalDateTime.now()), dbEntity, ProjectionTypeEntity::getMembershipTwinClassId, ProjectionTypeEntity::setMembershipTwinClassId, ProjectionTypeEntity.Fields.membershipTwinClassId, changesHelper);
            changes.add(dbEntity, changesHelper);
        }
        updateSafe(changes);

        return allEntities;
    }

    public KitGrouped<ProjectionTypeEntity, UUID, UUID> findAndGroupByTwinClassId(Set<UUID> groupIds) throws ServiceException {
        List<ProjectionTypeEntity> projections = projectionTypeRepository.findByProjectionTypeGroupIdIn(groupIds);

        if (projections.isEmpty()) {
            return KitGrouped.EMPTY;
        }

        UUID domainId = authService.getApiUser().getDomainId();

        List<ProjectionTypeEntity> filteredProjections = projections.stream().filter(projection -> domainId.equals(projection.getDomainId())).toList();

        if (filteredProjections.isEmpty()) {
            return KitGrouped.EMPTY;
        }

        return new KitGrouped<>(
                filteredProjections,
                ProjectionTypeEntity::getId,
                ProjectionTypeEntity::getMembershipTwinClassId
        );
    }
}
