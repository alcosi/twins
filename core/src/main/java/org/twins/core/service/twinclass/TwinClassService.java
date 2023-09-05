package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dao.twinclass.TwinClassSchemaRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassService {
    final TwinClassRepository twinClassRepository;
    final TwinClassSchemaRepository twinClassSchemaRepository;

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

    public void checkTwinClassPermission(ApiUser apiUser, UUID twinclassId) {

    }
}

