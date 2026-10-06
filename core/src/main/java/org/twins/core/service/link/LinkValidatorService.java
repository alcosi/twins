package org.twins.core.service.link;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.link.LinkValidatorEntity;
import org.twins.core.dao.link.LinkValidatorRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.linker.Linker;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.UUID;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class LinkValidatorService {
    private final LinkValidatorRepository linkValidatorRepository;
    private final FeaturerService featurerService;
    private final LinkService linkService;
    private final TwinClassService twinClassService;
    @Lazy // TwinService -> TwinLinkService -> this service
    private final TwinService twinService;
    @Lazy
    private final TwinSearchService twinSearchService;

    /**
     * Load pattern: validators are lazy-loaded into {@link LinkEntity#getLinkValidators()} once
     * (an empty list also counts as loaded — no repeated repository hits), same as
     * {@link LinkService#loadTwinClasses(LinkEntity)}.
     */
    public void loadLinkValidators(LinkEntity linkEntity) {
        if (linkEntity.getLinkValidators() != null)
            return;
        linkEntity.setLinkValidators(linkValidatorRepository.findByLinkIdOrderByOrder(linkEntity.getId()));
    }

    public PaginationResult<TwinEntity> findValidDstTwins(UUID twinClassId, UUID linkId, UUID headTwinId, BasicSearch basicSearch, SimplePagination pagination) throws ServiceException {
        LinkEntity linkEntity = linkService.findEntitySafe(linkId);
        TwinClassEntity srcTwinClassEntity = twinClassService.findEntitySafe(twinClassId);
        TwinEntity headTwinEntity = null;
        if (headTwinId != null)
            headTwinEntity = twinService.findEntitySafe(headTwinId);
        addClassCheckToValidTwinsForLinkSearch(linkEntity, srcTwinClassEntity, basicSearch);
        loadLinkValidators(linkEntity);
        for (LinkValidatorEntity linkValidatorEntity : linkEntity.getLinkValidators()) {
            Linker linker = featurerService.getFeaturer(linkValidatorEntity.getLinkerFeaturerId(), Linker.class);
            linker.expandValidLinkedTwinSearch(linkValidatorEntity.getLinkerParams(), srcTwinClassEntity, headTwinEntity, basicSearch);
        }
        return twinSearchService.findTwins(basicSearch, pagination);
    }

    public PaginationResult<TwinEntity> findValidDstTwins(UUID twinId, UUID linkId, BasicSearch basicSearch, SimplePagination pagination) throws ServiceException {
        LinkEntity linkEntity = linkService.findEntitySafe(linkId);
        TwinEntity twinEntity = twinService.findEntitySafe(twinId);
        addClassCheckToValidTwinsForLinkSearch(linkEntity, twinEntity.getTwinClass(), basicSearch);
        loadLinkValidators(linkEntity);
        for (LinkValidatorEntity linkValidatorEntity : linkEntity.getLinkValidators()) {
            Linker linker = featurerService.getFeaturer(linkValidatorEntity.getLinkerFeaturerId(), Linker.class);
            linker.expandValidLinkedTwinSearch(linkValidatorEntity.getLinkerParams(), false, twinEntity, basicSearch);
        }
        return twinSearchService.findTwins(basicSearch, pagination);
    }

    public void validateLinkByLinkers(LinkEntity linkEntity, boolean forwardElseBackward, TwinEntity twinEntity, UUID candidateTwinId) throws ServiceException {
        loadLinkValidators(linkEntity);
        if (CollectionUtils.isEmpty(linkEntity.getLinkValidators()))
            return;
        // internal validation, not a user-facing search — the candidate must not be filtered out by the caller's view permission
        BasicSearch basicSearch = new BasicSearch()
                .setCheckViewPermission(false);
        basicSearch.addTwinId(candidateTwinId, false); // narrow the search to the candidate twin only
        for (LinkValidatorEntity linkValidatorEntity : linkEntity.getLinkValidators()) {
            Linker linker = featurerService.getFeaturer(linkValidatorEntity.getLinkerFeaturerId(), Linker.class);
            linker.validateLink(linkValidatorEntity.getLinkerParams(), forwardElseBackward, twinEntity, basicSearch);
        }
        if (basicSearch.isEmptyResult() || twinSearchService.count(basicSearch) == 0) // existence check only — no twin hydration
            throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT,
                    linkEntity.logNormal() + " twinId[" + candidateTwinId + "] is not a valid dst twin for twinId[" + twinEntity.getId() + "] (linker validation failed)");
    }

    private void addClassCheckToValidTwinsForLinkSearch(LinkEntity linkEntity, TwinClassEntity srcTwinClass, BasicSearch search) throws ServiceException {
        if (linkService.isForwardLink(linkEntity, srcTwinClass)) {// forward link
            twinClassService.loadExtendsHierarchyChildClasses(linkEntity.getDstTwinClass());
            search.addTwinClassId(linkEntity.getDstTwinClass().getExtendsHierarchyChildClassKit().getIdSet(), false);
        } else if (linkService.isBackwardLink(linkEntity, srcTwinClass)) {// backward link
            twinClassService.loadExtendsHierarchyChildClasses(srcTwinClass);
            search.addTwinClassId(srcTwinClass.getExtendsHierarchyChildClassKit().getIdSet(), false);
        } else
            throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, "unknown link type");
    }
}
