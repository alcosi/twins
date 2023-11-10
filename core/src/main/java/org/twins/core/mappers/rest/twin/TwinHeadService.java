package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.BasicSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.user.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class TwinHeadService {
    final EntitySmartService entitySmartService;
    final TwinRepository twinRepository;
    @Lazy
    final TwinService twinService;
    final TwinClassService twinClassService;
    final SystemEntityService systemEntityService;
    final AuthService authService;
    final UserService userService;
    final BusinessAccountService businessAccountService;

    public List<TwinEntity> findValidHeads(TwinClassEntity twinClassEntity) throws ServiceException {
        if (twinClassEntity.getHeadTwinClassId() == null)
            return new ArrayList<>();
        BasicSearch basicSearch = new BasicSearch()
                .addTwinClassId(twinClassEntity.getHeadTwinClassId());
        if (twinClassEntity.getDomainId() != null) {
            TwinClassEntity headTwinClassEntity = twinClassService.findEntitySafe(twinClassEntity.getHeadTwinClassId());
            if (headTwinClassEntity.getDomainId() == null) {// out-of-domain head class. Valid twins list must be limited
                Set<UUID> twinIdSet = null;
                if (headTwinClassEntity.getId().equals(systemEntityService.getTwinClassIdForUser())) {// twin.id = user.id
                    twinIdSet = userService.getValidUserIdSetByTwinClass(twinClassEntity);
                } else if (headTwinClassEntity.getId().equals(systemEntityService.getTwinClassIdForBusinessAccount())) {// twin.id = business_account_id
                    twinIdSet = businessAccountService.getValidBusinessAccountIdSetByTwinClass(twinClassEntity);
                }
                if (CollectionUtils.isNotEmpty(twinIdSet))
                    basicSearch.setTwinIdList(twinIdSet);
            }
        }
        // todo create headHunterFeaturer for filtering twins by other fields (statuses, fields and so on)
        return twinService.findTwins(basicSearch);
    }

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
}
