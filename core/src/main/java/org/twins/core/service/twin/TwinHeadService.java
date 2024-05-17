package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.PaginationUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.pagination.SimplePagination;
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

    public TwinSearchResult findValidHeads(TwinClassEntity twinClassEntity, SimplePagination pagination) throws ServiceException {
        if (twinClassEntity.getHeadTwinClassId() == null)
            return convertListInDefaultSearchResult(pagination);
        Pageable pageable = PaginationUtils.pageableOffset(pagination);
        TwinClassEntity headTwinClassEntity = twinClassService.findEntitySafe(twinClassEntity.getHeadTwinClassId());
        HeadHunter headHunter = featurerService.getFeaturer(headTwinClassEntity.getHeadHunterFeaturer(), HeadHunter.class);
        if (headTwinClassEntity.getOwnerType().isSystemLevel()) {// out-of-domain head class. Valid twins list must be limited
            if (SystemEntityService.isTwinClassForUser(headTwinClassEntity.getId())) {// twin.id = user.id
                Page<TwinEntity> validUserTwinList = getValidUserTwinListByTwinClass(twinClassEntity, pageable);
                return twinSearchService.convertPageInTwinSearchResult(validUserTwinList, pagination);
            } else if (SystemEntityService.isTwinClassForBusinessAccount(headTwinClassEntity.getId())) {// twin.id = business_account_id
                Page<TwinEntity> validBusinessAccountTwinList = getValidBusinessAccountTwinListByTwinClass(twinClassEntity, pageable);
                return twinSearchService.convertPageInTwinSearchResult(validBusinessAccountTwinList, pagination);
            }
            log.warn(headTwinClassEntity.logShort() + " unknown system twin class for head");
        }
        return headHunter.findValidHead(headTwinClassEntity.getHeadHunterParams(), headTwinClassEntity, pageable);
    }

    private TwinSearchResult convertListInDefaultSearchResult(SimplePagination pagination) {
        return (TwinSearchResult) new TwinSearchResult()
                .setTwinList(new ArrayList<>())
                .setTotal(0)
                .setOffset(pagination.getOffset())
                .setLimit(pagination.getLimit());
    }

    public TwinSearchResult findValidHeads(UUID twinClassId, SimplePagination simplePagination) throws ServiceException {
        TwinClassEntity twinClassEntity = twinClassService.findEntitySafe(twinClassId);
        if (twinClassEntity == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_ID_UNKNOWN, "unknown head twin class id");
        return findValidHeads(twinClassEntity, simplePagination);
    }

    //todo cache it
    public Map<UUID, TwinEntity> findValidHeadsAsMap(TwinClassEntity twinClassEntity, SimplePagination pagination) throws ServiceException {
        List<TwinEntity> validHeads = findValidHeads(twinClassEntity, pagination).getTwinList();
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

    public Page<TwinEntity> getValidUserTwinListByTwinClass(TwinClassEntity twinClassEntity, Pageable pageable) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Page<TwinEntity> userTwinList = null;
        switch (twinClassEntity.getOwnerType()) {
            case DOMAIN_BUSINESS_ACCOUNT:
            case DOMAIN_BUSINESS_ACCOUNT_USER:
                // only users linked to domain and businessAccount at once will be selected
                userTwinList = twinHeadRepository.findUserTwinByBusinessAccountIdAndDomainId(apiUser.getBusinessAccount().getId(), apiUser.getDomain().getId(), pageable);
                break;
            case BUSINESS_ACCOUNT:
                // only users linked to businessAccount will be selected
                userTwinList = twinHeadRepository.findUserTwinByBusinessAccountId(apiUser.getBusinessAccount().getId(), pageable);
                break;
            case DOMAIN_USER:
            case DOMAIN:
                // only users linked to domain will be selected
                userTwinList = twinHeadRepository.findUserTwinByDomainId(apiUser.getDomain().getId(), pageable);
                break;
            case USER:
                //todo all users
        }
        return userTwinList;
    }

    public Page<TwinEntity> getValidBusinessAccountTwinListByTwinClass(TwinClassEntity twinClassEntity, Pageable pageable) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Page<TwinEntity> businessAccountList = null;
        switch (twinClassEntity.getOwnerType()) {
            case DOMAIN_BUSINESS_ACCOUNT:
            case DOMAIN_BUSINESS_ACCOUNT_USER:
                businessAccountList = twinHeadRepository.findBusinessAccountTwinByUserIdAndDomainId(apiUser.getUser().getId(), apiUser.getDomain().getId(), pageable);
                break;
            case USER:
                businessAccountList = twinHeadRepository.findBusinessAccountTwinByUser(apiUser.getUser().getId(), pageable);
                break;
            case DOMAIN_USER:
            case DOMAIN:
                businessAccountList = twinHeadRepository.findBusinessAccountTwinByDomainId(apiUser.getDomain().getId(), pageable);
                break;
        }
        return businessAccountList;
    }
}
