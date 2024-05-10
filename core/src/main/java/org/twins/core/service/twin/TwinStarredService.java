package org.twins.core.service.twin;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinStarredEntity;
import org.twins.core.dao.twin.TwinStarredRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinStarredService extends EntitySecureFindServiceImpl<TwinStarredEntity> {

    final TwinStarredRepository twinStarredRepository;
    final EntitySmartService entitySmartService;

    @Override
    public CrudRepository<TwinStarredEntity, UUID> entityRepository() {
        return null;
    }

    @Override
    public boolean isEntityReadDenied(TwinStarredEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinStarredEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return false;
    }

    public List<TwinStarredEntity> findStarred(UUID twinClassId) throws ServiceException {
        List<TwinStarredEntity> twinStarredListByTwinClassId = twinStarredRepository.findTwinStarredListByTwinClassId(twinClassId);
        if (twinStarredListByTwinClassId == null)
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN);
        return twinStarredListByTwinClassId;
    }

    public TwinStarredEntity addStarred(UUID twinId, ApiUser apiUser) throws ServiceException {
        TwinStarredEntity twinStarredEntity = new TwinStarredEntity()
                .setUserId(apiUser.getUserId())
                .setTwinId(twinId);
        try {
            twinStarredEntity = entitySmartService.save(twinStarredEntity, twinStarredRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        } catch (DataIntegrityViolationException e) {
            throw new ServiceException(ErrorCodeTwins.ENTITY_ALREADY_EXIST, "entity is already exist in db. Please check unique keys");
        }
        return twinStarredEntity;
    }

    @Transactional
    public void deleteStarred(UUID twinId, ApiUser apiUser) throws ServiceException {
        UUID userId = apiUser.getUserId();
        twinStarredRepository.deleteByTwinIdAndUserId(twinId, apiUser.getUserId());
        log.info("Starred[" + StringUtils.join(twinId, ",", userId) + "] perhaps were deleted");
    }
}
