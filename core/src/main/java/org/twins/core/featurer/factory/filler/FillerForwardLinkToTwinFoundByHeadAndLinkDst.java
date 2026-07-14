package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.service.twin.TwinSearchServiceV2;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Featurer(id = FeaturerTwins.ID_2357,
        name = "Forward link to twin found by head",
        description = "Finds twin by head and creates forward link from output twin to found twin. " +
                "Also searches uncommitted factory items from the current run.")
@Slf4j
public class FillerForwardLinkToTwinFoundByHeadAndLinkDst extends FillerLinks {

    @FeaturerParam(name = "New links id", description = "Forward link id to create from output twin", order = 1)
    public static final FeaturerParamUUID newLinksId = new FeaturerParamUUIDTwinsLinkId("newLinksId");

    @FeaturerParam(name = "Twin class id", description = "Twin class to search", order = 2)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");

    @FeaturerParam(name = "Exclude factory input twin", description = "Exclude context and factory input twins from search", order = 3, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeFactoryInputTwin = new FeaturerParamBoolean("excludeFactoryInputTwin");

    @FeaturerParam(name = "Resolve head root", description = "Resolve head from factory item, else from context twin", order = 4, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean factoryItemElseContext = new FeaturerParamBoolean("factoryItemElseContext");

    private final TwinSearchServiceV2 twinSearchService;

    public FillerForwardLinkToTwinFoundByHeadAndLinkDst(TwinSearchServiceV2 twinSearchService) {
        this.twinSearchService = twinSearchService;
    }

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwin = factoryItem.getTwin();
        if (outputTwin == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory output twin is empty");
        }

        TwinEntity foundTwin = findTwin(properties, factoryItem)
                .orElseThrow(() -> new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Twin of class[" + twinClassId.extract(properties) + "] not found by head"));

        LinkEntity link = linkService.findEntitySafe(newLinksId.extract(properties));
        TwinLinkEntity newLink = new TwinLinkEntity()
                .setLink(link)
                .setLinkId(link.getId())
                .setSrcTwinId(outputTwin.getId())
                .setSrcTwin(outputTwin)
                .setDstTwin(foundTwin)
                .setDstTwinId(foundTwin.getId());
        addLink(factoryItem.getOutput(), newLink);
    }

    private Optional<TwinEntity> findTwin(Properties properties, FactoryItem factoryItem) throws ServiceException {
        TwinEntity rootTwin;
        if (factoryItemElseContext.extract(properties)) {
            rootTwin = factoryItem.getTwin();
        } else {
            rootTwin = factoryItem.checkSingleContextTwin();
        }
        UUID headTwinId = rootTwin.getHeadTwinId() != null ? rootTwin.getHeadTwinId() : rootTwin.getId();
        if (headTwinId == null) {
            log.info("Context twin has no head, twin found by head search skipped");
            return Optional.empty();
        }

        UUID extractedTwinClassId = twinClassId.extract(properties);
        Set<UUID> excludeIds = buildExcludeIds(properties, factoryItem, rootTwin);

        List<TwinEntity> uncommittedMatches = findUncommittedMatchesByHead(factoryItem, extractedTwinClassId, headTwinId, excludeIds);
        if (uncommittedMatches.size() > 1) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "More than one uncommitted twin of class[" + extractedTwinClassId + "] found by head");
        }
        if (uncommittedMatches.size() == 1) {
            return Optional.of(uncommittedMatches.getFirst());
        }
        return findSingleDbMatchByHead(properties, factoryItem, headTwinId);
    }

    private List<TwinEntity> findUncommittedMatchesByHead(FactoryItem factoryItem, UUID extractedTwinClassId, UUID headTwinId, Set<UUID> excludeIds) {
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
            if (headTwinId.equals(candidateHeadTwinId)) {
                matches.add(candidateTwin);
            }
        }
        return matches;
    }

    private Optional<TwinEntity> findSingleDbMatchByHead(Properties properties, FactoryItem factoryItem, UUID headTwinId) throws ServiceException {
        BasicSearch search = new BasicSearch().setCheckViewPermission(false);
        search
                .addTwinClassId(twinClassId.extract(properties), false)
                .addHeadTwinId(headTwinId);

        if (excludeFactoryInputTwin.extract(properties)) {
            TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
            Set<UUID> excludeIds = buildExcludeIds(properties, factoryItem, contextTwin);
            if (!excludeIds.isEmpty()) {
                search.setTwinIdExcludeList(excludeIds);
            }
        }

        PaginationResult<TwinEntity> searchResult = twinSearchService.search(search, new SimplePagination().setOffset(0).setLimit(2));
        List<TwinEntity> dbMatches = searchResult.getList();
        if (dbMatches.isEmpty()) {
            return Optional.empty();
        }
        if (dbMatches.size() > 1) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "More than one twin of class[" + twinClassId.extract(properties) + "] found by head");
        }
        return Optional.of(dbMatches.getFirst());
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
