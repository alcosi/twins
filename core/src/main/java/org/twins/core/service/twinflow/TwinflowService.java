package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowRepository;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaRepository;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.UUIDCheckService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowService {
    final TwinflowRepository twinflowRepository;
    final TwinflowSchemaRepository twinflowSchemaRepository;
    final UUIDCheckService uuidCheckService;

    public UUID checkTwinflowSchemaAllowed(UUID domainId, UUID businessAccountId, UUID twinFlowsSchemaId) throws ServiceException {
        Optional<TwinflowSchemaEntity> permissionSchemaEntity = twinflowSchemaRepository.findById(twinFlowsSchemaId);
        if (permissionSchemaEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.INCORRECT_UUID, "unknown twinFlowsSchemaId[" + twinFlowsSchemaId + "]");
        if (permissionSchemaEntity.get().domainId() != domainId)
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_SCHEMA_NOT_ALLOWED, "twinFlowsSchemaId[" + twinFlowsSchemaId + "] is not allows in domain[" + domainId + "]");
        if (permissionSchemaEntity.get().businessAccountId() != null && permissionSchemaEntity.get().businessAccountId() != businessAccountId)
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_SCHEMA_NOT_ALLOWED, "twinFlowsSchemaId[" + twinFlowsSchemaId + "] is not allows in businessAccount[" + businessAccountId + "]");
        return twinFlowsSchemaId;
    }

}

