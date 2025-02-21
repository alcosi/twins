package org.twins.core.service.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainEntity;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationEntity;
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationRepository;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class PermissionGrantAssigneePropagationService extends EntitySecureFindServiceImpl<PermissionGrantAssigneePropagationEntity> {
    @Getter
    private final PermissionGrantAssigneePropagationRepository repository;
    private final PermissionSchemaService permissionSchemaService;
    private final PermissionService permissionService;
    private final AuthService authService;
    private final TwinClassService twinClassService;
    private final TwinStatusService twinStatusService;
    private final UserService userService;

    @Override
    public CrudRepository<PermissionGrantAssigneePropagationEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<PermissionGrantAssigneePropagationEntity, UUID> entityGetIdFunction() {
        return PermissionGrantAssigneePropagationEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(PermissionGrantAssigneePropagationEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = !entity.getTwinClass().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(PermissionGrantAssigneePropagationEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getPermissionSchema() == null || !entity.getPermissionSchema().getId().equals(entity.getPermissionSchemaId()))
                    entity.setPermissionSchema(permissionSchemaService.findEntitySafe(entity.getPermissionSchemaId()));
                if (entity.getPermission() == null || !entity.getPermission().getId().equals(entity.getPermissionId()))
                    entity.setPermission(permissionService.findEntitySafe(entity.getPermissionId()));
                if (entity.getTwinClass() == null || !entity.getTwinClass().getId().equals(entity.getPropagationByTwinClassId()))
                    entity.setTwinClass(twinClassService.findEntitySafe(entity.getPropagationByTwinClassId()));
                if (entity.getTwinStatus() == null || !entity.getTwinStatus().getId().equals(entity.getPropagationByTwinStatusId()))
                    entity.setTwinStatus(twinStatusService.findEntitySafe(entity.getPropagationByTwinStatusId()));
                if (entity.getGrantedByUser() == null || !entity.getGrantedByUser().getId().equals(entity.getGrantedByUserId()))
                    entity.setGrantedByUser(userService.findEntitySafe(entity.getGrantedByUserId()));
        }
        return true;
    }

    public PermissionGrantAssigneePropagationEntity createPermissionGrantAssigneePropagationEntity(PermissionGrantAssigneePropagationEntity entity) throws ServiceException {
        entity
                .setGrantedByUserId(authService.getApiUser().getUserId())
                .setGrantedAt(Timestamp.from(Instant.now()));
        return saveSafe(entity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public PermissionGrantAssigneePropagationEntity updatePermissionGrantAssigneePropagationEntity(PermissionGrantAssigneePropagationEntity entity) throws ServiceException {
        PermissionGrantAssigneePropagationEntity dbEntity = findEntitySafe(entity.getId());
        ChangesHelper changesHelper = new ChangesHelper();

        updatePermissionSchemaId(dbEntity, entity.getPermissionSchemaId(), changesHelper);
        updatePermissionId(dbEntity, entity.getPermissionId(), changesHelper);
        updatePropagationByTwinClassId(dbEntity, entity.getPropagationByTwinClassId(), changesHelper);
        updatePropagationByTwinStatusId(dbEntity, entity.getPropagationByTwinStatusId(), changesHelper);
        updateInSpaceOnly(dbEntity, entity.isInSpaceOnly(), changesHelper);

        return updateSafe(entity, changesHelper);
    }

    private void updatePermissionSchemaId(PermissionGrantAssigneePropagationEntity dbEntity, UUID newPermissionSchemaId,
                                          ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(PermissionGrantAssigneePropagationEntity.Fields.permissionSchemaId, dbEntity.getPermissionSchemaId(), newPermissionSchemaId))
            return;
        dbEntity.setPermissionSchemaId(newPermissionSchemaId);
    }

    private void updatePermissionId(PermissionGrantAssigneePropagationEntity dbEntity, UUID newPermissionId,
                                    ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(PermissionGrantAssigneePropagationEntity.Fields.permissionId, dbEntity.getPermissionId(), newPermissionId))
            return;
        dbEntity.setPermissionId(newPermissionId);
    }

    private void updatePropagationByTwinClassId(PermissionGrantAssigneePropagationEntity dbEntity, UUID newPropagationByTwinClassId,
                                                ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(PermissionGrantAssigneePropagationEntity.Fields.propagationByTwinClassId, dbEntity.getPropagationByTwinClassId(), newPropagationByTwinClassId))
            return;
        dbEntity.setPropagationByTwinClassId(newPropagationByTwinClassId);
    }

    private void updatePropagationByTwinStatusId(PermissionGrantAssigneePropagationEntity dbEntity, UUID newPropagationByTwinStatusId,
                                                 ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(PermissionGrantAssigneePropagationEntity.Fields.propagationByTwinStatusId, dbEntity.getPermissionSchemaId(), newPropagationByTwinStatusId))
            return;
        dbEntity.setPermissionSchemaId(newPropagationByTwinStatusId);
    }

    private void updateInSpaceOnly(PermissionGrantAssigneePropagationEntity dbEntity, Boolean newInSpaceOnly,
                                   ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(PermissionGrantAssigneePropagationEntity.Fields.inSpaceOnly, dbEntity.isInSpaceOnly(), newInSpaceOnly))
            return;
        dbEntity.setInSpaceOnly(newInSpaceOnly);
    }

    @Transactional
    public void deleteById(UUID id) throws ServiceException {
        //todo need validate???
        entitySmartService.deleteAndLog(id, repository);
    }
}