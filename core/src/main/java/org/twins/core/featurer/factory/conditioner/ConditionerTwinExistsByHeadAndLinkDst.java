package org.twins.core.featurer.factory.conditioner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
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

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Featurer(id = FeaturerTwins.ID_2446,
        name = "Twin exists by head and link dst",
        description = "True if twin exists with same head twin and link dst twin.")
@Slf4j
@RequiredArgsConstructor
public class ConditionerTwinExistsByHeadAndLinkDst extends Conditioner {

    @FeaturerParam(name = "Twin class id", description = "Twin class to search", order = 1)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");

    @FeaturerParam(name = "Dst link id", description = "Link id for search by link dst twin", order = 2)
    public static final FeaturerParamUUID dstLinkId = new FeaturerParamUUIDTwinsLinkId("dstLinkId");

    @FeaturerParam(name = "Dst twin class field id", description = "Field to read link dst twin id from context (link field or transition field)", order = 3, optional = true)
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    @FeaturerParam(name = "Exclude factory input twin", description = "Exclude context and factory input twins from search", order = 4, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeFactoryInputTwin = new FeaturerParamBoolean("excludeFactoryInputTwin");

    @FeaturerParam(name = "Match factory item output twin", description = "If true, checks that factory item output twin matches search (for multiplier filter on TwinUpdate)", order = 5, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean matchFactoryItemOutputTwin = new FeaturerParamBoolean("matchFactoryItemOutputTwin");

    private final TwinSearchServiceV2 twinSearchService;
    private final TwinLinkService twinLinkService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        Optional<BasicSearch> search = buildSearch(properties, factoryItem);
        if (search.isEmpty()) {
            return false;
        }
        return twinSearchService.exists(search.get());
    }

    private Optional<BasicSearch> buildSearch(Properties properties, FactoryItem factoryItem) throws ServiceException {
        TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
        Optional<UUID> headTwinId = Optional.ofNullable(contextTwin.getHeadTwinId())
                .or(() -> Optional.ofNullable(contextTwin.getId()));
        if (headTwinId.isEmpty()) {
            log.debug("Context twin has no head, twin exists by head and link dst search skipped");
            return Optional.empty();
        }

        Optional<UUID> dstTwinId = resolveDstTwinId(properties, factoryItem, contextTwin);
        if (dstTwinId.isEmpty()) {
            log.debug("Link dst twin id is not resolved, twin exists by head and link dst search skipped");
            return Optional.empty();
        }

        return assembleSearch(properties, factoryItem, contextTwin, headTwinId.get(), dstTwinId.get());
    }

    private Optional<BasicSearch> assembleSearch(Properties properties, FactoryItem factoryItem, TwinEntity contextTwin, UUID headTwinId, UUID dstTwinId) {
        BasicSearch search = new BasicSearch().setCheckViewPermission(false);
        search
                .addTwinClassId(twinClassId.extract(properties), false)
                .addHeadTwinId(headTwinId)
                .addLinkDstTwinsId(dstLinkId.extract(properties), List.of(dstTwinId), false, true);

        if (excludeFactoryInputTwin.extract(properties)) {
            Set<UUID> excludeIds = factoryItem.getFactoryContext().getInputTwinList().stream()
                    .map(TwinEntity::getId)
                    .collect(Collectors.toSet());
            if (contextTwin.getId() != null) {
                excludeIds.add(contextTwin.getId());
            }
            if (!excludeIds.isEmpty()) {
                search.setTwinIdExcludeList(excludeIds);
            }
        }

        if (matchFactoryItemOutputTwin.extract(properties)) {
            return Optional.ofNullable(factoryItem.getOutput().getTwinEntity().getId())
                    .map(outputTwinId -> {
                        search.addTwinId(outputTwinId, false);
                        return search;
                    })
                    .or(() -> {
                        log.debug("Factory item output twin has no id, twin exists by head and link dst match skipped");
                        return Optional.empty();
                    });
        }

        return Optional.of(search);
    }

    private Optional<UUID> resolveDstTwinId(Properties properties, FactoryItem factoryItem, TwinEntity contextTwin) throws ServiceException {
        UUID dstFieldId = dstTwinClassFieldId.extract(properties);
        if (dstFieldId != null) {
            FieldValue dstFieldValue = fieldLookupers.getFromContextFieldsAndContextTwinDbFields()
                    .lookupFieldValue(factoryItem, dstFieldId);
            Optional<UUID> dstTwinId = extractTwinIdFromFieldValue(dstFieldValue);
            if (dstTwinId.isPresent()) {
                return dstTwinId;
            }
        }
        return resolveDstTwinIdFromLink(contextTwin, dstLinkId.extract(properties));
    }

    private Optional<UUID> resolveDstTwinIdFromLink(TwinEntity contextTwin, UUID linkId) throws ServiceException {
        twinLinkService.loadTwinLinks(contextTwin);
        List<TwinLinkEntity> forwardLinks = contextTwin.getTwinLinks().getForwardLinks().getGrouped(linkId);
        if (CollectionUtils.isEmpty(forwardLinks)) {
            log.debug("Link dst twin not found by link [{}] on context twin [{}]", linkId, contextTwin.logShort());
            return Optional.empty();
        }
        TwinLinkEntity linkEntity = forwardLinks.getFirst();
        if (linkEntity.getDstTwin() != null) {
            return Optional.ofNullable(linkEntity.getDstTwin().getId());
        }
        return Optional.ofNullable(linkEntity.getDstTwinId());
    }

    private Optional<UUID> extractTwinIdFromFieldValue(FieldValue fieldValue) {
        if (fieldValue instanceof FieldValueLinkSingle linkSingle && linkSingle.isNotEmpty()) {
            return Optional.ofNullable(linkSingle.getValue().getId());
        }
        if (fieldValue instanceof FieldValueLink link && link.isNotEmpty()) {
            TwinLinkEntity linkEntity = link.getItems().getFirst();
            if (linkEntity.getDstTwin() != null) {
                return Optional.ofNullable(linkEntity.getDstTwin().getId());
            }
            return Optional.ofNullable(linkEntity.getDstTwinId());
        }
        if (fieldValue instanceof FieldValueText textField && textField.isNotEmpty()) {
            String value = textField.getValue().trim();
            if (UuidUtils.isUUID(value)) {
                return Optional.of(UUID.fromString(value));
            }
        }
        return Optional.empty();
    }
}
