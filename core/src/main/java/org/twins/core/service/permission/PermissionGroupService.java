package org.twins.core.service.permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.permission.PermissionGroupRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionGroupService extends EntitySecureFindServiceImpl<PermissionGroupEntity> {
    final PermissionGroupRepository permissionGroupRepository;
    final AuthService authService;

    @Override
    public CrudRepository<PermissionGroupEntity, UUID> entityRepository() {
        return permissionGroupRepository;
    }

    @Override
    public boolean isEntityReadDenied(PermissionGroupEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        if (entity.getDomainId() != null && !entity.getDomainId().equals(authService.getApiUser().getDomainId()))
            return true;
        return false;
    }

    @Override
    public boolean validateEntity(PermissionGroupEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (entity.getKey() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty key");
        switch (entityValidateMode) {
            default:
                if (apiUser.isDomainSpecified() && entity.getDomainId() != null && !entity.getDomainId().equals(apiUser.getDomainId()))
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect domainId");
        }
        return true;
    }

    //todo когда аннотация Lazy у поля permissionGroup, не работают как пологается методы loadPermissionGroup
//    public void loadPermissionGroup(PermissionEntity entity) {
//        loadPermissionGroup(Collections.singletonList(entity));
//    }

//    public void loadPermissionGroup(Collection<PermissionEntity> permissionList) {
//        if (CollectionUtils.isEmpty(permissionList))
//            return;
//        KitGrouped<PermissionEntity, UUID, UUID> needLoad = new KitGrouped<>(PermissionEntity::getId, PermissionEntity::getPermissionGroupId);
//        for (PermissionEntity permission : permissionList)
//            if (permission.getPermissionGroupLoaded() == null)
//                needLoad.add(permission);
//        if (needLoad.isEmpty())
//            return;
//        List<PermissionGroupEntity> permissionGroupEntities = permissionGroupRepository.findAllByIdIn(needLoad.getGroupedMap().keySet());
//        for (PermissionGroupEntity permissionGroup : permissionGroupEntities)
//            for (PermissionEntity permission : needLoad.getGroupedMap().get(permissionGroup.getId()))
//                permission.setPermissionGroupLoaded(permissionGroup);
//    }
}
