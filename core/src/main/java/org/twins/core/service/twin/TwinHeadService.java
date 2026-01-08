package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
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
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.headhunter.HeadHunter;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Lazy
@RequiredArgsConstructor
public class TwinHeadService {
    @Lazy
    private final TwinClassService twinClassService;
    @Lazy
    private final TwinService twinService;
    private final FeaturerService featurerService;
    private final TwinSearchService twinSearchService;

    public PaginationResult<TwinEntity> findValidHeads(TwinClassEntity twinClassEntity, BasicSearch basicSearch, SimplePagination pagination) throws ServiceException {
        if (twinClassEntity.getHeadTwinClassId() == null)
            return PaginationUtils.convertInPaginationResult(pagination);
        TwinClassEntity headTwinClassEntity = twinClassService.findEntitySafe(twinClassEntity.getHeadTwinClassId());
        if (headTwinClassEntity.getOwnerType().isSystemLevel()) {// out-of-domain head class. Valid twins list must be limited
            if (SystemEntityService.isTwinClassForUser(headTwinClassEntity.getId()) // twin.id = user.id
                    || SystemEntityService.isTwinClassForBusinessAccount(headTwinClassEntity.getId())) { // twin.id = business_account_id
                basicSearch.addTwinClassId(headTwinClassEntity.getId(), false);// DBU check depends on class for which we are searching heads
            } else {
                throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, headTwinClassEntity.logShort() + " unknown system twin class for head");
            }
        } else {
            basicSearch.addTwinClassExtendsHierarchyContainsId(headTwinClassEntity.getId());
        }
        if (twinClassEntity.getHeadHunterFeaturerId() != null) {//headhunter should not be empty if head twin is specified and head class is not USER and BA
            HeadHunter headHunter = featurerService.getFeaturer(twinClassEntity.getHeadHunterFeaturerId(), HeadHunter.class);
            headHunter.expandValidHeadSearch(twinClassEntity.getHeadHunterParams(), twinClassEntity, basicSearch);
        }
        //todo add checkSegmentUniq logic, to exclude heads with segments
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
        var head = validHead.getList().getFirst();
        checkSegmentUniq(head, subClass);
        return head;
    }

    private void checkSegmentUniq(TwinEntity headTwin, TwinClassEntity subClass) throws ServiceException {
        if (Boolean.FALSE.equals(subClass.getSegment()))
            return;
        //segments are one-to-one only
        var segmentsSearch = new BasicSearch();
        segmentsSearch
                .addTwinClassId(subClass.getId(), false)
                .addHeadTwinId(headTwin.getId());
        if (twinSearchService.exists(segmentsSearch)) {
            throw new ServiceException(ErrorCodeTwins.HEAD_TWIN_SEGMENT_NOT_UNIQ, "segment of {} is already exists for head {} ", subClass.logShort(), headTwin.logShort());
        }
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
                if (childTwinClassEntity.getHeadHunterFeaturerId() == null)
                    continue;
                HeadHunter headHunter = featurerService.getFeaturer(childTwinClassEntity.getHeadHunterFeaturerId(), HeadHunter.class);
                if (headHunter.isCreatableChildClass(childTwinClassEntity.getHeadHunterParams(), twinEntity, childTwinClassEntity)) {
                    //todo check permission
                    creatableChildTwinClasses.add(childTwinClassEntity);
                }
            }
            twinEntity.setCreatableChildTwinClasses(creatableChildTwinClasses);
        }
    }
}
