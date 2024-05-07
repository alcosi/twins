package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.PaginationUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinHeadRepository;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.twinclass.HeadHunter;
import org.twins.core.service.EntitySmartService;
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
    final FeaturerService featurerService;

    private TwinSearchResult findValidHeads(TwinClassEntity twinClassEntity, int offset, int limit) throws ServiceException {
        if (twinClassEntity.getHeadTwinClassId() == null)
            return (TwinSearchResult) new TwinSearchResult()
                    .setTwinList(new ArrayList<>())
                    .setOffset(offset)
                    .setLimit(limit)
                    .setTotal(0);
        TwinClassEntity headTwinClassEntity = twinClassService.findEntitySafe(twinClassEntity.getHeadTwinClassId());
        HeadHunter headHunter = featurerService.getFeaturer(headTwinClassEntity.getHeadHunterFeaturer(), HeadHunter.class);
        if (headHunter == null)
            throw new ServiceException(ErrorCodeTwins.FEATURER_UNKNOWN, "featurer is unknown");
        return headHunter.findValidHead(headTwinClassEntity.getHeadHunterParams(), headTwinClassEntity, PaginationUtils.paginationOffsetUnsorted(offset, limit));
    }

    public TwinSearchResult findValidHeads(UUID twinClassId, int offset, int limit) throws ServiceException {
        TwinClassEntity twinClassEntity = twinClassService.findEntitySafe(twinClassId);
        if (twinClassEntity == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_ID_UNKNOWN, "unknown head twin class id");
        return findValidHeads(twinClassEntity, offset, limit);
    }

    //todo cache it
    public Map<UUID, TwinEntity> findValidHeadsAsMap(TwinClassEntity twinClassEntity, int offset, int limit) throws ServiceException {
        List<TwinEntity> validHeads = findValidHeads(twinClassEntity, offset, limit).getTwinList();
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
