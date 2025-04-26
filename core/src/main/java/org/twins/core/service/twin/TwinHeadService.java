package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGroupedObj;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinHeadRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.headhunter.HeadHunter;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DBUService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class TwinHeadService {
    private final TwinHeadRepository twinHeadRepository;
    @Lazy
    private final TwinClassService twinClassService;
    @Lazy
    private final TwinService twinService;
    private final AuthService authService;
    private final FeaturerService featurerService;
    private final TwinSearchService twinSearchService;
    private final DBUService dbuService;

    public PaginationResult<TwinEntity> findValidHeads(TwinClassEntity twinClassEntity, BasicSearch basicSearch, SimplePagination pagination) throws ServiceException {
        if (twinClassEntity.getHeadTwinClassId() == null)
            return PaginationUtils.convertInPaginationResult(pagination);
        TwinClassEntity headTwinClassEntity = twinClassService.findEntitySafe(twinClassEntity.getHeadTwinClassId());
        if (!headTwinClassEntity.getOwnerType().isSystemLevel()) {// out-of-domain head class. Valid twins list must be limited
            if (SystemEntityService.isTwinClassForUser(headTwinClassEntity.getId()) // twin.id = user.id
                    || SystemEntityService.isTwinClassForBusinessAccount(headTwinClassEntity.getId())) { // twin.id = business_account_id
                basicSearch
                        .addTwinClassId(headTwinClassEntity.getId(), false)
                        .setDbuMembershipCheck(dbuService.detectSystemTwinsDBUMembershipCheck(twinClassEntity.getOwnerType()));// DBU check depends on class for which we are searching heads
            } else {
                throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, headTwinClassEntity.logShort() + " unknown system twin class for head");
            }
        } else {
            //head can be configured as parent class
            twinClassService.loadExtendsHierarchyChildClasses(headTwinClassEntity);
            basicSearch.addTwinClassId(headTwinClassEntity.getExtendsHierarchyChildClassKit().getIdSet(), false);
        }
        if (twinClassEntity.getHeadHunterFeaturer() != null) {//headhunter should not be empty if head twin is specified and head class is not USER and BA
            HeadHunter headHunter = featurerService.getFeaturer(twinClassEntity.getHeadHunterFeaturer(), HeadHunter.class);
            headHunter.expandValidHeadSearch(twinClassEntity.getHeadHunterParams(), twinClassEntity, basicSearch);
        }
        return twinSearchService.findTwins(basicSearch, pagination);
    }

    public PaginationResult<TwinEntity> findValidHeadsByClass(UUID twinClassId, BasicSearch basicSearch, SimplePagination simplePagination) throws ServiceException {
        TwinClassEntity twinClassEntity = twinClassService.findEntitySafe(twinClassId);
        return findValidHeads(twinClassEntity, basicSearch, simplePagination);
    }

    public PaginationResult<TwinEntity> findValidHeadsByTwin(UUID twinId, BasicSearch basicSearch, SimplePagination pagination) throws ServiceException {
        TwinEntity twinEntity = twinService.findEntitySafe(twinId);
        return findValidHeads(twinEntity.getTwinClass(), basicSearch, pagination);
    }

    public TwinEntity checkValidHeadForClass(UUID headTwinId, TwinClassEntity subClass) throws ServiceException {
        if (subClass.getHeadTwinClassId() == null)
            throw new ServiceException(ErrorCodeTwins.HEAD_TWIN_ID_NOT_ALLOWED, "No head class configured for " + subClass.logShort());
        if (headTwinId == null)
            throw new ServiceException(ErrorCodeTwins.HEAD_TWIN_NOT_SPECIFIED, subClass.easyLog(EasyLoggable.Level.NORMAL) + " should be linked to head");
        BasicSearch basicSearch = new BasicSearch();
        basicSearch.addTwinId(headTwinId, false);
        PaginationResult<TwinEntity> validHead = findValidHeads(subClass, basicSearch, SimplePagination.SINGLE);
        if (validHead.getTotal() == 0)
            throw new ServiceException(ErrorCodeTwins.HEAD_TWIN_ID_NOT_ALLOWED, "twin[" + headTwinId + "] is not allowed for twinClass[" + subClass.getId() + "]");
        return validHead.getList().getFirst();
    }

    //todo delete me
    @Deprecated
    public Page<TwinEntity> getValidUserTwinListByTwinClass(TwinClassEntity twinClassEntity, SimplePagination pagination) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Pageable pageable = PaginationUtils.pageableOffset(pagination);
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

    //todo delete me
    @Deprecated
    public Page<TwinEntity> getValidBusinessAccountTwinListByTwinClass(TwinClassEntity twinClassEntity, SimplePagination pagination) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Pageable pageable = PaginationUtils.pageableOffset(pagination);
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

    public void loadCreatableChildTwinClasses(TwinEntity twinEntity) throws ServiceException {
        loadCreatableChildTwinClasses(Collections.singletonList(twinEntity));
    }

    public void loadCreatableChildTwinClasses(Collection<TwinEntity> twinEntityCollection) throws ServiceException {
        KitGroupedObj<TwinEntity, UUID, UUID, TwinClassEntity> needLoad = new KitGroupedObj<>(TwinEntity::getId, TwinEntity::getTwinClassId, TwinEntity::getTwinClass);
        for (TwinEntity twinEntity : twinEntityCollection) {
            if (twinEntity.getCreatableChildTwinClasses() != null)
                continue;
            needLoad.add(twinEntity);
        }
        twinClassService.loadHeadHierarchyChildClasses(needLoad.getGroupingObjectMap().values());
        for (TwinEntity twinEntity : needLoad.getList()) {
            Kit<TwinClassEntity, UUID> creatableChildTwinClasses = new Kit<>(TwinClassEntity::getId);
            for (TwinClassEntity childTwinClassEntity : twinEntity.getTwinClass().getHeadHierarchyChildClassKit().getList()) {
                if (childTwinClassEntity.getHeadHunterFeaturer() == null)
                    continue;
                HeadHunter headHunter = featurerService.getFeaturer(childTwinClassEntity.getHeadHunterFeaturer(), HeadHunter.class);
                if (headHunter.isCreatableChildClass(childTwinClassEntity.getHeadHunterParams(), twinEntity, childTwinClassEntity)) {
                    //todo check permission
                    creatableChildTwinClasses.add(childTwinClassEntity);
                }
            }
            twinEntity.setCreatableChildTwinClasses(creatableChildTwinClasses);
        }
    }
}
