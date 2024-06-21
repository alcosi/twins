package org.twins.core.service.twin;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.TypedParameterTwins;
import org.twins.core.dao.twin.TwinStarredEntity;
import org.twins.core.dao.twin.TwinStarredRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.UserGroupService;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinStarredService extends EntitySecureFindServiceImpl<TwinStarredEntity> {
    final AuthService authService;
    final TwinService twinService;
    final UserGroupService userGroupService;
    final TwinStarredRepository twinStarredRepository;

    public List<TwinStarredEntity> findStarred(UUID twinClassId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        userGroupService.loadGroups(apiUser);
        return twinStarredRepository.findTwinStarredListByTwinClassIdAndUserIdAndUserGroupId(
                        twinClassId,
                        apiUser.getDomainId(),
                        TypedParameterTwins.uuidNullable(apiUser.getBusinessAccountId()),
                        apiUser.getUserId(),
                        TypedParameterTwins.uuidArray(apiUser.getUserGroups()))
                .stream()
                .filter(Predicate.not(this::isEntityReadDenied))
                .collect(Collectors.toList());
    }

    public TwinStarredEntity addStarred(UUID twinId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        TwinStarredEntity twinStarredEntityFromDB = twinStarredRepository.findByTwinIdAndUserId(twinId, apiUser.getUserId());
        if (twinStarredEntityFromDB == null) {
            TwinStarredEntity twinStarredEntity = new TwinStarredEntity()
                    .setUserId(apiUser.getUserId())
                    .setTwinId(twinId);
            twinStarredEntityFromDB = entitySmartService.save(twinStarredEntity, twinStarredRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        }
        return twinStarredEntityFromDB;
    }

    @Transactional
    public void deleteStarred(UUID twinId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID userId = apiUser.getUserId();
        twinStarredRepository.deleteByTwinIdAndUserId(twinId, apiUser.getUserId());
        log.info("Starred[{}] perhaps were deleted", StringUtils.join(twinId, ",", userId));
    }

    @Override
    public CrudRepository<TwinStarredEntity, UUID> entityRepository() {
        return null;
    }

    @Override
    public boolean isEntityReadDenied(TwinStarredEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return twinService.isEntityReadDenied(entity.getTwin(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(TwinStarredEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
