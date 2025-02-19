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
import org.twins.core.dao.permission.PermissionGrantUserGroupEntity;
import org.twins.core.dao.permission.PermissionGrantUserGroupRepository;
import org.twins.core.service.auth.AuthService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class PermissionGrantUserGroupService extends EntitySecureFindServiceImpl<PermissionGrantUserGroupEntity> {
    @Getter
    private final PermissionGrantUserGroupRepository repository;
    private final AuthService authService;
    private final PermissionSchemaService permissionSchemaService;

    @Override
    public CrudRepository<PermissionGrantUserGroupEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<PermissionGrantUserGroupEntity, UUID> entityGetIdFunction() {
        return PermissionGrantUserGroupEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(PermissionGrantUserGroupEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = !entity.getPermissionSchema().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(PermissionGrantUserGroupEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getPermissionSchema() == null)
            entity.setPermissionSchema(permissionSchemaService.findEntitySafe(entity.getPermissionSchemaId()));
        return !isEntityReadDenied(entity, EntitySmartService.ReadPermissionCheckMode.none);
    }

    @Transactional(rollbackFor = Throwable.class)
    public PermissionGrantUserGroupEntity createPermissionGrantUserGroup(PermissionGrantUserGroupEntity createEntity) throws ServiceException {
        createEntity
                .setGrantedByUserId(authService.getApiUser().getUserId())
                .setGrantedAt(Timestamp.from(Instant.now()));
        validateEntityAndThrow(createEntity, EntitySmartService.EntityValidateMode.beforeSave);
        return repository.save(createEntity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public PermissionGrantUserGroupEntity updatePermissionGrantUserGroup(PermissionGrantUserGroupEntity updateEntity) throws ServiceException {
        PermissionGrantUserGroupEntity dbEntity = findEntitySafe(updateEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateFieldEntityId(updateEntity, dbEntity, PermissionGrantUserGroupEntity::getPermissionSchemaId, PermissionGrantUserGroupEntity::setPermissionSchemaId,
                PermissionGrantUserGroupEntity.Fields.permissionSchemaId, changesHelper);
        updateFieldEntityId(updateEntity, dbEntity, PermissionGrantUserGroupEntity::getPermissionId, PermissionGrantUserGroupEntity::setPermissionId,
                PermissionGrantUserGroupEntity.Fields.permissionId, changesHelper);
        updateFieldEntityId(updateEntity, dbEntity, PermissionGrantUserGroupEntity::getUserGroupId, PermissionGrantUserGroupEntity::setUserGroupId,
                PermissionGrantUserGroupEntity.Fields.userGroupId, changesHelper);
        return updateSafe(dbEntity, changesHelper);
    }

    private void updateFieldEntityId(PermissionGrantUserGroupEntity updateEntity, PermissionGrantUserGroupEntity dbEntity,
                                     Function<PermissionGrantUserGroupEntity, UUID> getFunction, BiConsumer<PermissionGrantUserGroupEntity, UUID> setFunction,
                                     String field, ChangesHelper changesHelper) {
        UUID updateValue = getFunction.apply(updateEntity);
        UUID dbValue = getFunction.apply(dbEntity);
        if (!changesHelper.isChanged(field, dbValue, updateValue)) {
            return;
        }
        setFunction.accept(dbEntity, updateValue);
    }
}
