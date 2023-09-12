package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dao.twinclass.TwinClassSchemaRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySmartService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassService {
    final TwinRepository twinRepository;
    final TwinClassRepository twinClassRepository;
    final TwinClassSchemaRepository twinClassSchemaRepository;
    final EntitySmartService entitySmartService;

    public List<TwinClassEntity> findTwinClasses(ApiUser apiUser, List<UUID> uuidLists) {
        if (CollectionUtils.isNotEmpty(uuidLists))
            return twinClassRepository.findByDomainIdAndIdIn(apiUser.domainId(), uuidLists);
        else
            return twinClassRepository.findByDomainId(apiUser.domainId());
    }

    public TwinClassEntity findTwinClass(ApiUser apiUser, UUID twinClassIs) {
        return twinClassRepository.findByDomainIdAndId(apiUser.domainId(), twinClassIs);
    }

    public UUID checkTwinClassSchemaAllowed(UUID domainId, UUID twinClassSchemaId) throws ServiceException {
            Optional<TwinClassSchemaEntity> twinClassSchemaEntity = twinClassSchemaRepository.findById(twinClassSchemaId);
        if (twinClassSchemaEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown twinClassSchemaId[" + twinClassSchemaId + "]");
        if (twinClassSchemaEntity.get().domainId() != domainId)
            throw new ServiceException(ErrorCodeTwins.PERMISSION_SCHEMA_NOT_ALLOWED, "twinClassSchemaId[" + twinClassSchemaId + "] is not allows in domain[" + domainId + "]");
        return twinClassSchemaId;
    }

    public UUID checkSpaceTwinAllowedForClass(UUID spaceTwinId, UUID inSpaceTwinClassId) throws ServiceException {
        if (spaceTwinId != null) {
            TwinEntity spaceTwinEntity = entitySmartService.findById(spaceTwinId, "spaceTwinId", twinRepository, EntitySmartService.FindMode.ifEmptyThrows);
            if (!spaceTwinEntity.twinClass().space())
                throw new ServiceException(ErrorCodeTwins.SPACE_TWIN_ID_INCORRECT, spaceTwinEntity.logShort() + " is not a space");
            if (!spaceTwinEntity.twinClassId().equals(inSpaceTwinClassId))
                throw new ServiceException(ErrorCodeTwins.SPACE_TWIN_ID_NOT_ALLOWED, spaceTwinEntity.logShort() + " is not allowed for twinClass[" + inSpaceTwinClassId + "]");
            return spaceTwinId;
        } else {
            TwinClassEntity twinClassEntity = entitySmartService.findById(inSpaceTwinClassId, "twinClassId", twinClassRepository, EntitySmartService.FindMode.ifEmptyThrows);
            if (twinClassEntity.spaceTwinClassId() != null)
                throw new ServiceException(ErrorCodeTwins.SPACE_TWIN_NOT_SPECIFIED, twinClassEntity.logShort() + " should be linked to space");
        }
        return spaceTwinId;
    }

    public void checkTwinClassPermission(ApiUser apiUser, UUID twinclassId) {

    }
}

