package org.cambium.service;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.twins.core.domain.usage.Usage;
import org.twins.core.enums.usage.UsageType;

import java.util.*;

public interface EntitySecureFindService<T> {
    UUID checkId(UUID id, EntitySmartService.CheckMode checkMode) throws ServiceException;
    T findEntity(UUID entityId,
                 EntitySmartService.FindMode findMode,
                 EntitySmartService.ReadPermissionCheckMode permissionCheckMode,
                 EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException;
    T findEntity(String key,
                 EntitySmartService.FindMode findMode,
                 EntitySmartService.ReadPermissionCheckMode permissionCheckMode,
                 EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException;
    Kit<T, UUID> findEntities(Collection<UUID> entityId,
                              EntitySmartService.ListFindMode findMode,
                              EntitySmartService.ReadPermissionCheckMode permissionCheckMode,
                              EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException;
    boolean isEntityReadDenied(T entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException;
    boolean validateEntity(T entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException;

    default boolean validateEntities(Collection<T> entities, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        beforeValidateEntities(entities, entityValidateMode);
        for (T entity : entities) {
            if (!validateEntity(entity, entityValidateMode)) return false;
        }
        return true;
    }

    default void beforeValidateEntity(T entity, EntitySmartService.EntityValidateMode entityValidateMode) {
        beforeValidateEntities(Collections.singletonList(entity), entityValidateMode);
    }

    void beforeValidateEntities(Collection<T> entities, EntitySmartService.EntityValidateMode entityValidateMode);

    static void addUsage(Map<UUID, List<Usage>> usagesMap, UUID usageOf, UsageType usageType, UUID usageEntityId, Object usageEntity) {
        if (usageOf == null)
            return;
        List<Usage> usages = usagesMap.computeIfAbsent(usageOf, k -> new ArrayList<>());
        usages.add(new Usage().setUsageType(usageType).setId(usageEntityId).setEntity(usageEntity));
    }
}
