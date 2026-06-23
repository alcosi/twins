package org.twins.core.featurer.factory.filler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.UuidUtils;
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
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueLinkSingle;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinSearchServiceV2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Featurer(id = FeaturerTwins.ID_2357,
        name = "Forward link to twin found by head and link dst",
        description = "Finds twin by head and link dst. Creates forward link from output twin to found twin. " +
                "Also searches uncommitted factory items from the current run.")
@Slf4j
@RequiredArgsConstructor
public class FillerForwardLinkToTwinFoundByHeadAndLinkDst extends FillerLinks {

    @FeaturerParam(name = "New links id", description = "Forward link id to create from output twin", order = 1)
    public static final FeaturerParamUUID newLinksId = new FeaturerParamUUIDTwinsLinkId("newLinksId");

    @FeaturerParam(name = "Twin class id", description = "Twin class to search", order = 2)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");

    @FeaturerParam(name = "Dst link id", description = "Link id for search by link dst twin. If omitted, twin is found by head only", order = 3, optional = true)
    public static final FeaturerParamUUID dstLinkId = new FeaturerParamUUIDTwinsLinkId("dstLinkId");

    @FeaturerParam(name = "Dst twin class field id", description = "Field to read link dst twin id from context (link field or transition field)", order = 4, optional = true)
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    @FeaturerParam(name = "Exclude factory input twin", description = "Exclude context and factory input twins from search", order = 5, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeFactoryInputTwin = new FeaturerParamBoolean("excludeFactoryInputTwin");

    private final TwinSearchServiceV2 twinSearchService;
    private final TwinLinkService twinLinkService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwin = factoryItem.getTwin();
        if (outputTwin == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory output twin is empty");
        }

        TwinEntity foundTwin = findTwin(properties, factoryItem)
                .orElseThrow(() -> new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Twin of class[" + twinClassId.extract(properties) + "] not found by head and link dst"));

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
        TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
        UUID headTwinId = contextTwin.getHeadTwinId() != null ? contextTwin.getHeadTwinId() : contextTwin.getId();
        if (headTwinId == null) {
            log.info("Context twin has no head, twin found by head and link dst search skipped");
            return Optional.empty();
        }

        UUID extractedTwinClassId = twinClassId.extract(properties);
        UUID extractedDstLinkId = dstLinkId.extract(properties);
        Set<UUID> excludeIds = buildExcludeIds(properties, factoryItem, contextTwin);

        if (extractedDstLinkId == null) {
            List<TwinEntity> uncommittedMatches = findUncommittedMatchesByHead(factoryItem, extractedTwinClassId, headTwinId, excludeIds);
            if (uncommittedMatches.size() > 1) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "More than one uncommitted twin of class[" + extractedTwinClassId + "] found by head");
            }
            if (uncommittedMatches.size() == 1) {
                return Optional.of(uncommittedMatches.getFirst());
            }
            return findSingleDbMatchByHead(properties, factoryItem, headTwinId);
        }

        UUID dstTwinId = resolveDstTwinId(properties, factoryItem, contextTwin);
        if (dstTwinId == null) {
            log.info("Link dst twin id is not resolved, twin found by head and link dst search skipped");
            return Optional.empty();
        }

        List<TwinEntity> uncommittedMatches = findUncommittedMatches(factoryItem, extractedTwinClassId, headTwinId, extractedDstLinkId, dstTwinId, excludeIds);
        if (uncommittedMatches.size() > 1) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "More than one uncommitted twin of class[" + extractedTwinClassId + "] found by head and link dst");
        }
        if (uncommittedMatches.size() == 1) {
            return Optional.of(uncommittedMatches.getFirst());
        }

        Optional<BasicSearch> search = buildDbSearch(properties, factoryItem, headTwinId, dstTwinId);
        if (search.isEmpty()) {
            return Optional.empty();
        }
        PaginationResult<TwinEntity> searchResult = twinSearchService.search(search.get(), new SimplePagination().setOffset(0).setLimit(2));
        List<TwinEntity> dbMatches = searchResult.getList();
        if (dbMatches.isEmpty()) {
            return Optional.empty();
        }
        if (dbMatches.size() > 1) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "More than one twin of class[" + extractedTwinClassId + "] found by head and link dst");
        }
        return Optional.of(dbMatches.getFirst());
    }

    private List<TwinEntity> findUncommittedMatches(
            FactoryItem factoryItem,
            UUID extractedTwinClassId,
            UUID headTwinId,
            UUID extractedDstLinkId,
            UUID dstTwinId,
            Set<UUID> excludeIds
    ) {
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
            if (hasLinkDst(candidateOutput, extractedDstLinkId, dstTwinId)) {
                matches.add(candidateTwin);
            }
        }
        return matches;
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

    private boolean hasLinkDst(TwinOperation twinOperation, UUID linkId, UUID dstTwinId) {
        if (twinOperation instanceof TwinCreate twinCreate) {
            for (TwinLinkEntity linkEntity : twinCreate.getLinksEntityList()) {
                if (!linkId.equals(linkEntity.getLinkId())) {
                    continue;
                }
                UUID linkedDstTwinId = linkEntity.getDstTwinId() != null
                        ? linkEntity.getDstTwinId()
                        : linkEntity.getDstTwin() != null ? linkEntity.getDstTwin().getId() : null;
                if (dstTwinId.equals(linkedDstTwinId)) {
                    return true;
                }
            }
        } else if (twinOperation instanceof TwinUpdate twinUpdate && twinUpdate.getTwinLinkCUD() != null
                && twinUpdate.getTwinLinkCUD().getCreateList() != null) {
            for (TwinLinkEntity linkEntity : twinUpdate.getTwinLinkCUD().getCreateList()) {
                if (!linkId.equals(linkEntity.getLinkId())) {
                    continue;
                }
                UUID linkedDstTwinId = linkEntity.getDstTwinId() != null
                        ? linkEntity.getDstTwinId()
                        : linkEntity.getDstTwin() != null ? linkEntity.getDstTwin().getId() : null;
                if (dstTwinId.equals(linkedDstTwinId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Optional<BasicSearch> buildDbSearch(Properties properties, FactoryItem factoryItem, UUID headTwinId, UUID dstTwinId) throws ServiceException {
        BasicSearch search = new BasicSearch().setCheckViewPermission(false);
        search
                .addTwinClassId(twinClassId.extract(properties), false)
                .addHeadTwinId(headTwinId)
                .addLinkDstTwinsId(dstLinkId.extract(properties), List.of(dstTwinId), false, true);

        if (excludeFactoryInputTwin.extract(properties)) {
            TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
            Set<UUID> excludeIds = buildExcludeIds(properties, factoryItem, contextTwin);
            if (!excludeIds.isEmpty()) {
                search.setTwinIdExcludeList(excludeIds);
            }
        }
        return Optional.of(search);
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

    private UUID resolveDstTwinId(Properties properties, FactoryItem factoryItem, TwinEntity contextTwin) throws ServiceException {
        UUID dstFieldId = dstTwinClassFieldId.extract(properties);
        if (dstFieldId != null) {
            FieldValue dstFieldValue = fieldLookupers.getFromContextFieldsAndContextTwinDbFields().lookupFieldValue(factoryItem, dstFieldId);
            UUID dstTwinId = extractTwinIdFromFieldValue(dstFieldValue);
            if (dstTwinId != null) {
                return dstTwinId;
            }
        }

        UUID linkId = dstLinkId.extract(properties);
        twinLinkService.loadTwinLinks(contextTwin);
        try {
            return contextTwin.getTwinLinks().getForwardLinks().getGrouped(linkId).getFirst().getDstTwin().getId();
        } catch (Exception e) {
            log.info("Link dst twin not found by link [{}] on context twin [{}]", linkId, contextTwin.logShort());
            return null;
        }
    }

    private UUID extractTwinIdFromFieldValue(FieldValue fieldValue) {
        if (fieldValue instanceof FieldValueLinkSingle linkSingle && linkSingle.isNotEmpty()) {
            return linkSingle.getValue().getId();
        }
        if (fieldValue instanceof FieldValueLink link && link.isNotEmpty()) {
            TwinLinkEntity linkEntity = link.getItems().getFirst();
            if (linkEntity.getDstTwin() != null) {
                return linkEntity.getDstTwin().getId();
            }
            return linkEntity.getDstTwinId();
        }
        if (fieldValue instanceof FieldValueText textField && textField.isNotEmpty()) {
            String value = textField.getValue().trim();
            if (UuidUtils.isUUID(value)) {
                return UUID.fromString(value);
            }
        }
        return null;
    }
}
