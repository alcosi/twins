package org.twins.core.service.permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.permission.PermissionSchemaRepository;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySmartService;


import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {
    final TwinClassRepository twinClassRepository;
    final PermissionSchemaRepository permissionSchemaRepository;

    public UUID checkPermissionSchemaAllowed(UUID domainId, UUID businessAccountId, UUID permissionSchemaId) throws ServiceException {
        Optional<PermissionSchemaEntity> permissionSchemaEntity = permissionSchemaRepository.findById(permissionSchemaId);
        if (permissionSchemaEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown permissionSchemaId[" + permissionSchemaId + "]");
        if (permissionSchemaEntity.get().domainId() != domainId)
            throw new ServiceException(ErrorCodeTwins.PERMISSION_SCHEMA_NOT_ALLOWED, "permissionSchemaId[" + permissionSchemaId + "] is not allows in domain[" + domainId + "]");
        if (permissionSchemaEntity.get().businessAccountId() != null && permissionSchemaEntity.get().businessAccountId() != businessAccountId)
            throw new ServiceException(ErrorCodeTwins.PERMISSION_SCHEMA_NOT_ALLOWED, "permissionSchemaId[" + permissionSchemaId + "] is not allows in businessAccount[" + businessAccountId + "]");
        return permissionSchemaId;
    }

    public void checkTwinClassPermission(ApiUser apiUser, UUID twinClassId) {

    }
}

