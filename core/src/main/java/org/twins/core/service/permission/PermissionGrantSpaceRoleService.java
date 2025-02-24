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
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.permission.PermissionGrantSpaceRoleEntity;
import org.twins.core.dao.permission.PermissionGrantSpaceRoleRepository;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.space.SpaceRoleService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class PermissionGrantSpaceRoleService extends EntitySecureFindServiceImpl<PermissionGrantSpaceRoleEntity> {
    @Getter
    private final PermissionGrantSpaceRoleRepository repository;
    private final AuthService authService;
    private final PermissionSchemaService permissionSchemaService;
    private final PermissionService permissionService;
    private final SpaceRoleService spaceRoleService;
    private final UserService userService;

    @Override
    public CrudRepository<PermissionGrantSpaceRoleEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<PermissionGrantSpaceRoleEntity, UUID> entityGetIdFunction() {
        return PermissionGrantSpaceRoleEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(PermissionGrantSpaceRoleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = !entity.getPermissionSchema().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(PermissionGrantSpaceRoleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getPermissionSchema() == null || !entity.getPermissionSchema().getId().equals(entity.getPermissionSchemaId()))
                    entity.setPermissionSchema(permissionSchemaService.findEntitySafe(entity.getPermissionSchemaId()));
                if (entity.getPermission() == null || !entity.getPermission().getId().equals(entity.getPermissionId()))
                    entity.setPermission(permissionService.findEntitySafe(entity.getPermissionId()));
                if (entity.getSpaceRole() == null || !entity.getSpaceRole().getId().equals(entity.getSpaceRoleId()))
                    entity.setSpaceRole(spaceRoleService.findEntitySafe(entity.getSpaceRoleId()));
                if (entity.getGrantedByUserId() != null && (entity.getGrantedByUser() == null || !entity.getGrantedByUser().getId().equals(entity.getGrantedByUserId())))
                    entity.setGrantedByUser(userService.findEntitySafe(entity.getGrantedByUserId()));
        }
        return true;
    }

    public PermissionGrantSpaceRoleEntity createPermissionGrantSpaceRole(PermissionGrantSpaceRoleEntity entity) throws ServiceException {
        entity
                .setGrantedByUserId(authService.getApiUser().getUserId())
                .setGrantedAt(Timestamp.from(Instant.now()));
        return saveSafe(entity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public PermissionGrantSpaceRoleEntity updatePermissionGrantSpaceRole(PermissionGrantSpaceRoleEntity entity) throws ServiceException {
        PermissionGrantSpaceRoleEntity dbEntity = findEntitySafe(entity.getId());
        ChangesHelper changesHelper = new ChangesHelper();

        updatePermissionSchemaId(dbEntity, entity.getPermissionSchemaId(), changesHelper);
        updatePermissionId(dbEntity, entity.getPermissionId(), changesHelper);
        updateSpaceRoleId(dbEntity, entity.getSpaceRoleId(), changesHelper);
        return updateSafe(entity, changesHelper);
    }

    private void updatePermissionSchemaId(PermissionGrantSpaceRoleEntity dbEntity, UUID newPermissionSchemaId,
                                          ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(PermissionGrantSpaceRoleEntity.Fields.permissionSchemaId, dbEntity.getPermissionSchemaId(), newPermissionSchemaId))
            return;
        dbEntity.setPermissionSchemaId(newPermissionSchemaId);
    }

    private void updatePermissionId(PermissionGrantSpaceRoleEntity dbEntity, UUID newPermissionId,
                                    ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(PermissionGrantSpaceRoleEntity.Fields.permissionId, dbEntity.getPermissionId(), newPermissionId))
            return;
        dbEntity.setPermissionId(newPermissionId);
    }

    private void updateSpaceRoleId(PermissionGrantSpaceRoleEntity dbEntity, UUID newSpaceRoleId,
                                    ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(PermissionGrantSpaceRoleEntity.Fields.spaceRoleId, dbEntity.getSpaceRoleId(), newSpaceRoleId))
            return;
        dbEntity.setSpaceRoleId(newSpaceRoleId);
    }

    @Transactional
    public void deleteById(UUID id) throws ServiceException {
        //todo need validate???
        entitySmartService.deleteAndLog(id, repository);
    }
}
