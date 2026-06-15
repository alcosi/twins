package org.twins.core.service.permission;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionExportService extends EntityExportService<PermissionEntity> {
    private final PermissionService permissionService;

    @Override
    public String exportCollectionToSql(Collection<PermissionEntity> permissions) throws ServiceException {
        if (CollectionUtils.isEmpty(permissions)) return "";

        var sqlParts = new StringList();

        Set<UUID> i18nIds = i18nService.collectI18nIds(permissions,
                PermissionEntity::getNameI18NId,
                PermissionEntity::getDescriptionI18NId);
        i18nExportService.addExportSafe(i18nIds, sqlParts);

        sqlParts.addNotBlank(sqlBuilder.buildInserts(permissions));

        return String.join("\n", sqlParts);
    }

    public String exportToSql(Set<UUID> permissionIds) throws ServiceException {
        return exportCollectionToSql(permissionService.findEntitiesSafe(permissionIds).getList());
    }
}
