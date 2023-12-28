package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinHeadRepository;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.BasicSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.user.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class TwinHeadService {
    final EntitySmartService entitySmartService;
    final TwinRepository twinRepository;
    final TwinHeadRepository twinHeadRepository;
    @Lazy
    final TwinService twinService;
    final TwinSearchService twinSearchService;
    final TwinClassService twinClassService;
    final AuthService authService;
    final UserService userService;
    final BusinessAccountService businessAccountService;

    public List<TwinEntity> findValidHeads(TwinClassEntity twinClassEntity) throws ServiceException {
        if (twinClassEntity.getHeadTwinClassId() == null) //todo check parent
            return new ArrayList<>();
        BasicSearch basicSearch = new BasicSearch()
                .addTwinClassId(twinClassEntity.getHeadTwinClassId());
        if (twinClassEntity.getDomainId() != null) {
            TwinClassEntity headTwinClassEntity = twinClassService.findEntitySafe(twinClassEntity.getHeadTwinClassId());
            if (headTwinClassEntity.getOwnerType().isSystemLevel()) {// out-of-domain head class. Valid twins list must be limited
                if (SystemEntityService.isTwinClassForUser(headTwinClassEntity.getId())) {// twin.id = user.id
                    return getValidUserTwinListByTwinClass(twinClassEntity);
                } else if (SystemEntityService.isTwinClassForBusinessAccount(headTwinClassEntity.getId())) {// twin.id = business_account_id
                    return getValidBusinessAccountTwinListByTwinClass(twinClassEntity);
                }
                log.warn(headTwinClassEntity.logShort() + " unknown system twin class for head");
            }
        }
        // todo create headHunterFeaturer for filtering twins by other fields (statuses, fields and so on)
        return twinSearchService.findTwins(basicSearch);
    }

    //todo cache it
    public Map<UUID, TwinEntity> findValidHeadsAsMap(TwinClassEntity twinClassEntity) throws ServiceException {
        List<TwinEntity> validHeads = findValidHeads(twinClassEntity);
        return EntitySmartService.convertToMap(validHeads, TwinEntity::getId);
    }

    @Transactional
    public UUID checkHeadTwinAllowedForClass(UUID headTwinId, TwinClassEntity subClass) throws ServiceException {
        if (subClass.getHeadTwinClassId() != null)
            if (headTwinId != null) {
                TwinEntity headTwinEntity = entitySmartService.findById(headTwinId, twinRepository, EntitySmartService.FindMode.ifEmptyThrows);
                if (!headTwinEntity.getTwinClassId().equals(subClass.getHeadTwinClassId()))
                    throw new ServiceException(ErrorCodeTwins.HEAD_TWIN_ID_NOT_ALLOWED, headTwinEntity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed for twinClass[" + subClass.getId() + "]");
                return headTwinId;
            } else {
                throw new ServiceException(ErrorCodeTwins.HEAD_TWIN_NOT_SPECIFIED, subClass.easyLog(EasyLoggable.Level.NORMAL) + " should be linked to head");
            }
        return headTwinId;
    }

    public List<TwinEntity> getValidUserTwinListByTwinClass(TwinClassEntity twinClassEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        List<TwinEntity> userTwinList = null;
        switch (twinClassEntity.getOwnerType()) {
            case DOMAIN_BUSINESS_ACCOUNT:
            case DOMAIN_BUSINESS_ACCOUNT_USER:
                // only users linked to domain and businessAccount at once will be selected
                userTwinList = twinHeadRepository.findUserTwinByBusinessAccountIdAndDomainId(apiUser.getBusinessAccount().getId(), apiUser.getDomain().getId());
                break;
            case BUSINESS_ACCOUNT:
                // only users linked to businessAccount will be selected
                userTwinList = twinHeadRepository.findUserTwinByBusinessAccountId(apiUser.getBusinessAccount().getId());
                break;
            case DOMAIN_USER:
            case DOMAIN:
                // only users linked to domain will be selected
                userTwinList = twinHeadRepository.findUserTwinByDomainId(apiUser.getDomain().getId());
                break;
            case USER:
                //todo all users
        }
        return userTwinList;
    }

    public List<TwinEntity> getValidBusinessAccountTwinListByTwinClass(TwinClassEntity twinClassEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        List<TwinEntity> businessAccountList = null;
        switch (twinClassEntity.getOwnerType()) {
            case DOMAIN_BUSINESS_ACCOUNT:
            case DOMAIN_BUSINESS_ACCOUNT_USER:
                businessAccountList = twinHeadRepository.findBusinessAccountTwinByUserIdAndDomainId(apiUser.getUser().getId(), apiUser.getDomain().getId());
                break;
            case USER:
                businessAccountList = twinHeadRepository.findBusinessAccountTwinByUser(apiUser.getUser().getId());
                break;
            case DOMAIN_USER:
            case DOMAIN:
                businessAccountList = twinHeadRepository.findBusinessAccountTwinByDomainId(apiUser.getDomain().getId());
                break;
        }
        return businessAccountList;
    }
}
