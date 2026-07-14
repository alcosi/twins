package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.service.twin.TwinSearchServiceV2;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class FillerForwardLinkToTwinFoundByHeadAndLinkDstBase extends FillerLinks {

    @FeaturerParam(name = "New links id", description = "Forward link id to create from output twin", order = 1)
    public static final FeaturerParamUUID newLinksId = new FeaturerParamUUIDTwinsLinkId("newLinksId");

    @FeaturerParam(name = "Twin class id", description = "Twin class to search", order = 2)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");

    @FeaturerParam(name = "Exclude factory input twin", description = "Exclude context and factory input twins from search", order = 4, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeFactoryInputTwin = new FeaturerParamBoolean("excludeFactoryInputTwin");

    @Lazy
    @Autowired
    private TwinSearchServiceV2 twinSearchService;

    @Override
    public void fill(Properties properties, Collection<FactoryItem> factoryItems, TwinEntity templateTwin, boolean optional) throws ServiceException {
        // the link to create is step-constant (same newLinksId for every item) -> resolve it once per batch
        LinkEntity link = linkService.findEntitySafe(newLinksId.extract(properties));
        for (FactoryItem factoryItem : factoryItems) {
            try {
                fillItem(properties, factoryItem, link);
            } catch (Exception ex) {
                if (optional && canBeOptional()) {
                    log.warn("Optional filler step failed for {}, skipping: {}", factoryItem.logShort(), (ex instanceof ServiceException serviceException ? serviceException.getErrorLocation() : ex.getMessage()));
                } else {
                    throw ex;
                }
            }
        }
    }

    private void fillItem(Properties properties, FactoryItem factoryItem, LinkEntity link) throws ServiceException {
        TwinEntity outputTwin = factoryItem.getTwin();
        if (outputTwin == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory output twin is empty");
        }
        TwinEntity foundTwin = findTwin(properties, factoryItem)
                .orElseThrow(() -> new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR,
                        "Twin of class[" + twinClassId.extract(properties) + "] not found by head and link dst"));
        addLink(factoryItem.getOutput(), TwinLinkEntity.of(link, outputTwin, foundTwin));
    }

    private Optional<TwinEntity> findTwin(Properties properties, FactoryItem factoryItem) throws ServiceException {
        TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
        UUID headTwinId = contextTwin.getHeadTwinId() != null ? contextTwin.getHeadTwinId() : contextTwin.getId();
        if (headTwinId == null) {
            log.info("Context twin has no head, twin found by head and link dst search skipped");
            return Optional.empty();
        }

        UUID extractedTwinClassId = twinClassId.extract(properties);
        UUID linkId = getLinkId(properties);
        UUID dstTwinId = resolveDstTwinId(properties, factoryItem, contextTwin);
        if (dstTwinId == null) {
            log.info("Link dst twin id is not resolved, twin found by head and link dst search skipped");
            return Optional.empty();
        }

        Set<UUID> excludeIds = buildExcludeIds(properties, factoryItem, contextTwin);

        List<TwinEntity> uncommittedMatches = findUncommittedMatches(factoryItem, extractedTwinClassId, headTwinId, linkId, dstTwinId, excludeIds);
        if (uncommittedMatches.size() > 1) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR,
                    "More than one uncommitted twin of class[" + extractedTwinClassId + "] found by head and link dst");
        }
        if (uncommittedMatches.size() == 1) {
            return Optional.of(uncommittedMatches.getFirst());
        }

        BasicSearch search = buildDbSearch(properties, headTwinId, dstTwinId, linkId, excludeIds);
        PaginationResult<TwinEntity> searchResult = twinSearchService.search(search, new SimplePagination().setOffset(0).setLimit(2));
        List<TwinEntity> dbMatches = searchResult.getList();
        if (dbMatches.isEmpty()) {
            return Optional.empty();
        }
        if (dbMatches.size() > 1) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR,
                    "More than one twin of class[" + extractedTwinClassId + "] found by head and link dst");
        }
        return Optional.of(dbMatches.getFirst());
    }

    private BasicSearch buildDbSearch(Properties properties, UUID headTwinId, UUID dstTwinId, UUID linkId, Set<UUID> excludeIds) throws ServiceException {
        BasicSearch search = new BasicSearch().setCheckViewPermission(false);
        search
                .addTwinClassId(twinClassId.extract(properties), false)
                .addHeadTwinId(headTwinId);
        if (linkId != null) {
            search.addLinkDstTwinsId(linkId, List.of(dstTwinId), false, true);
        }

        if (!excludeIds.isEmpty()) {
            search.setTwinIdExcludeList(excludeIds);
        }
        return search;
    }

    protected abstract UUID resolveDstTwinId(Properties properties, FactoryItem factoryItem, TwinEntity contextTwin) throws ServiceException;

    protected abstract UUID getLinkId(Properties properties) throws ServiceException;

    private List<TwinEntity> findUncommittedMatches(FactoryItem factoryItem, UUID extractedTwinClassId, UUID headTwinId, UUID linkId, UUID dstTwinId, Set<UUID> excludeIds) {
        List<TwinEntity> matches = new ArrayList<>();
        for (FactoryItem candidateItem : factoryItem.getFactoryContext().getAllFactoryItemList()) {
            TwinOperation candidateOutput = candidateItem.getOutput();
            TwinEntity candidateTwin = candidateOutput.getTwinEntity();
            if (candidateTwin == null || candidateTwin.getId() == null) {
                continue;
            }
            if (excludeIds.contains(candidateTwin.getId())) {
                continue;
            }
            if (!extractedTwinClassId.equals(candidateTwin.getTwinClassId())) {
                continue;
            }
            UUID candidateHeadTwinId = candidateTwin.getHeadTwinId() != null ? candidateTwin.getHeadTwinId() : candidateTwin.getId();
            if (!headTwinId.equals(candidateHeadTwinId)) {
                continue;
            }
            if (linkId == null || hasLinkDst(candidateOutput, linkId, dstTwinId)) {
                matches.add(candidateTwin);
            }
        }
        return matches;
    }

    private boolean hasLinkDst(TwinOperation twinOperation, UUID linkId, UUID dstTwinId) {
        if (twinOperation instanceof TwinCreate twinCreate) {
            for (TwinLinkEntity linkEntity : twinCreate.getLinksEntityList()) {
                if (!linkId.equals(linkEntity.getLinkId())) {
                    continue;
                }
                UUID linkedDstTwinId = linkEntity.getDstTwinId() != null ? linkEntity.getDstTwinId()
                        : linkEntity.getDstTwin() != null ? linkEntity.getDstTwin().getId() : null;
                if (dstTwinId.equals(linkedDstTwinId)) {
                    return true;
                }
            }
        } else if (twinOperation instanceof TwinUpdate twinUpdate
                && twinUpdate.getTwinLinkCUD() != null
                && twinUpdate.getTwinLinkCUD().getCreateList() != null) {
            for (TwinLinkEntity linkEntity : twinUpdate.getTwinLinkCUD().getCreateList()) {
                if (!linkId.equals(linkEntity.getLinkId())) {
                    continue;
                }
                UUID linkedDstTwinId = linkEntity.getDstTwinId() != null ? linkEntity.getDstTwinId()
                        : linkEntity.getDstTwin() != null ? linkEntity.getDstTwin().getId() : null;
                if (dstTwinId.equals(linkedDstTwinId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Set<UUID> buildExcludeIds(Properties properties, FactoryItem factoryItem, TwinEntity contextTwin) throws ServiceException {
        if (!excludeFactoryInputTwin.extract(properties)) {
            return Set.of();
        }
        Set<UUID> excludeIds = factoryItem.getFactoryContext().getInputTwinList().stream()
                .map(TwinEntity::getId)
                .collect(Collectors.toSet());
        if (contextTwin.getId() != null) {
            excludeIds.add(contextTwin.getId());
        }
        return excludeIds;
    }
}

