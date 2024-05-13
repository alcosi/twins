package org.twins.core.service.twin;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinStarredEntity;
import org.twins.core.dao.twin.TwinStarredRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinStarredService {
    final AuthService authService;
    final TwinService twinService;
    final TwinStarredRepository twinStarredRepository;

    public List<TwinStarredEntity> findStarred(UUID twinClassId) throws ServiceException {
        return twinStarredRepository.findTwinStarredListByTwinClassId(twinClassId);
    }

    public TwinStarredEntity addStarred(UUID twinId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        TwinStarredEntity twinStarredEntityFromDB = twinStarredRepository.findByTwinIdAndUserId(twinId, apiUser.getUserId());
        if (twinStarredEntityFromDB == null) {
            TwinStarredEntity twinStarredEntity = new TwinStarredEntity()
                    .setUserId(apiUser.getUserId())
                    .setTwinId(twinId);
            twinStarredEntityFromDB = twinService.entitySmartService.save(twinStarredEntity, twinStarredRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        }
        return twinStarredEntityFromDB;
    }

    @Transactional
    public void deleteStarred(UUID twinId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID userId = apiUser.getUserId();
        twinStarredRepository.deleteByTwinIdAndUserId(twinId, apiUser.getUserId());
        log.info("Starred[" + StringUtils.join(twinId, ",", userId) + "] perhaps were deleted");
    }

}
