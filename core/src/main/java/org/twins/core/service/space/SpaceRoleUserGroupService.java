package org.twins.core.service.space;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.space.SpaceRoleUserGroupEntity;
import org.twins.core.dao.space.SpaceRoleUserGroupRepository;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.usergroup.UserGroupService;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class SpaceRoleUserGroupService extends TwinsEntitySecureFindService<SpaceRoleUserGroupEntity> {
    @Getter
    private final SpaceRoleUserGroupRepository repository;
    @Lazy
    private final TwinService twinService;
    @Lazy
    private final UserGroupService userGroupService;
    @Lazy
    private final SpaceRoleService spaceRoleService;

    @Override
    public CrudRepository<SpaceRoleUserGroupEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<SpaceRoleUserGroupEntity, UUID> entityGetIdFunction() {
        return SpaceRoleUserGroupEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(SpaceRoleUserGroupEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(SpaceRoleUserGroupEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadTwin(SpaceRoleUserGroupEntity src) throws ServiceException {
        loadTwin(Collections.singletonList(src));
    }

    public void loadTwin(Collection<SpaceRoleUserGroupEntity> srcCollection) throws ServiceException {
        twinService.load(srcCollection,
                SpaceRoleUserGroupEntity::getTwinId,
                SpaceRoleUserGroupEntity::getTwin,
                SpaceRoleUserGroupEntity::setTwin);
    }

    public void loadUserGroup(SpaceRoleUserGroupEntity src) throws ServiceException {
        loadUserGroup(Collections.singletonList(src));
    }

    public void loadUserGroup(Collection<SpaceRoleUserGroupEntity> srcCollection) throws ServiceException {
        userGroupService.load(srcCollection,
                SpaceRoleUserGroupEntity::getUserGroupId,
                SpaceRoleUserGroupEntity::getUserGroup,
                SpaceRoleUserGroupEntity::setUserGroup);
    }

    public void loadSpaceRole(SpaceRoleUserGroupEntity src) throws ServiceException {
        loadSpaceRole(Collections.singletonList(src));
    }

    public void loadSpaceRole(Collection<SpaceRoleUserGroupEntity> srcCollection) throws ServiceException {
        spaceRoleService.load(srcCollection,
                SpaceRoleUserGroupEntity::getSpaceRoleId,
                SpaceRoleUserGroupEntity::getSpaceRole,
                SpaceRoleUserGroupEntity::setSpaceRole);
    }
}
