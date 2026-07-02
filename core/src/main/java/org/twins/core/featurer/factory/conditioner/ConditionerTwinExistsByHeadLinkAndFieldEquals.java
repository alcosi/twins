package org.twins.core.featurer.factory.conditioner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinFieldClause;
import org.twins.core.domain.TwinFieldFilter;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.domain.search.TwinFieldValueSearch;
import org.twins.core.domain.search.TwinFieldValueSearchNumeric;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueLinkSingle;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsDataListOptionId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinSearchServiceV2;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Featurer(id = FeaturerTwins.ID_2443,
        name = "Twin exists by head, link and field equals",
        description = "True if twin exists with same head, dst link, numeric field value and optional dst twin assignee.")
@Slf4j
@RequiredArgsConstructor
public class ConditionerTwinExistsByHeadLinkAndFieldEquals extends Conditioner {

    @FeaturerParam(name = "Twin class id", description = "Twin class to search", order = 1)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");

    @FeaturerParam(name = "Dst link id", description = "Link id for search by link dst twin", order = 2)
    public static final FeaturerParamUUID dstLinkId = new FeaturerParamUUIDTwinsLinkId("dstLinkId");

    @FeaturerParam(name = "Dst twin class field id", description = "Field to read link dst twin id from context (link field or transition field)", order = 3, optional = true)
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    @FeaturerParam(name = "Equals twin class field id", description = "Numeric field for equals compare (e.g. price)", order = 4)
    public static final FeaturerParamUUID equalsTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("equalsTwinClassFieldId");

    @FeaturerParam(name = "Match assignee", description = "If true, add dst twin assignerUserId to search when set; ignore assignee when dst twin has none", order = 5, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean matchAssignee = new FeaturerParamBoolean("matchAssignee");

    @FeaturerParam(name = "Exclude factory input twin", description = "Exclude context and factory input twins from search", order = 6, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeFactoryInputTwin = new FeaturerParamBoolean("excludeFactoryInputTwin");

    @FeaturerParam(name = "Match factory item output twin", description = "If true, checks that factory item output twin matches search (for multiplier filter on TwinUpdate)", order = 7, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean matchFactoryItemOutputTwin = new FeaturerParamBoolean("matchFactoryItemOutputTwin");

    @FeaturerParam(name = "Flavor data list option id", description = "Optional twin flavor for location-specific uniqueness", order = 8, optional = true)
    public static final FeaturerParamUUID flavorDataListOptionId = new FeaturerParamUUIDTwinsDataListOptionId("flavorDataListOptionId");

    private final TwinSearchServiceV2 twinSearchService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinLinkService twinLinkService;
    private final TwinService twinService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        Optional<BasicSearch> search = buildSearch(properties, factoryItem);
        if (search.isEmpty()) {
            log.info("Unique twin search skipped");
            return false;
        }
        return twinSearchService.exists(search.get());
    }

    private Optional<BasicSearch> buildSearch(Properties properties, FactoryItem factoryItem) throws ServiceException {
        TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
        UUID headTwinId = contextTwin.getHeadTwinId() != null ? contextTwin.getHeadTwinId() : contextTwin.getId();
        if (headTwinId == null) {
            log.info("Context twin has no head, unique twin search skipped");
            return Optional.empty();
        }

        UUID dstTwinId = resolveDstTwinId(properties, factoryItem, contextTwin);
        if (dstTwinId == null) {
            log.info("Link dst twin id is not resolved, unique twin search skipped");
            return Optional.empty();
        }

        UUID equalsFieldId = equalsTwinClassFieldId.extract(properties);
        FieldValue equalsFieldValue = fieldLookupers.getFromContextFieldsAndContextTwinDbFields()
                .lookupFieldValue(factoryItem, equalsFieldId);
        if (!(equalsFieldValue instanceof FieldValueText priceField) || priceField.isEmpty()) {
            log.info("Equals field value is empty, unique twin search skipped");
            return Optional.empty();
        }

        double equalsValue;
        try {
            equalsValue = Double.parseDouble(priceField.getValue());
        } catch (NumberFormatException e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_VALUE_INCORRECT,
                    "Incorrect numeric value for field [" + equalsFieldId + "]: [" + priceField.getValue() + "]");
        }

        BasicSearch search = new BasicSearch().setCheckViewPermission(false);
        search
                .addTwinClassId(twinClassId.extract(properties), false)
                .addHeadTwinId(headTwinId)
                .addLinkDstTwinsId(dstLinkId.extract(properties), List.of(dstTwinId), false, true)
                .setFieldsFilter(new TwinFieldFilter()
                        .addClause(new TwinFieldClause()
                                .addCondition(buildNumericFieldEquals(equalsFieldId, equalsValue))));

        if (matchAssignee.extract(properties)) {
            UUID assigneeUserId = resolveAssigneeUserIdFromDstTwin(dstTwinId);
            if (assigneeUserId != null) {
                search.addAssigneeUserId(assigneeUserId, true);
            } else {
                log.info("Dst twin [{}] has no assignee, assignee ignored in unique twin search", dstTwinId);
            }
        }

        UUID flavorId = flavorDataListOptionId.extract(properties);
        if (flavorId != null) {
            search.addFlavorDataListOptionId(flavorId, false);
        }

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
            UUID outputTwinId = factoryItem.getOutput().getTwinEntity().getId();
            if (outputTwinId == null) {
                log.info("Factory item output twin has no id, unique twin match skipped");
                return Optional.empty();
            }
            search.addTwinId(outputTwinId, false);
        }

        return Optional.of(search);
    }

    private UUID resolveDstTwinId(Properties properties, FactoryItem factoryItem, TwinEntity contextTwin) throws ServiceException {
        UUID dstFieldId = dstTwinClassFieldId.extract(properties);
        if (dstFieldId != null) {
            FieldValue dstFieldValue = fieldLookupers.getFromContextFieldsAndContextTwinDbFields()
                    .lookupFieldValue(factoryItem, dstFieldId);
            UUID dstTwinId = extractTwinIdFromFieldValue(dstFieldValue);
            if (dstTwinId != null) {
                return dstTwinId;
            }
        }

        UUID linkId = dstLinkId.extract(properties);
        twinLinkService.loadTwinLinks(contextTwin);
        try {
            return contextTwin.getTwinLinks().getForwardLinks().getGrouped(linkId).getFirst().getDstTwinId();
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

    private UUID resolveAssigneeUserIdFromDstTwin(UUID dstTwinId) throws ServiceException {
        return twinService.findEntitySafe(dstTwinId).getAssignerUserId();
    }

    private TwinFieldSearch buildNumericFieldEquals(UUID fieldId, double value) throws ServiceException {
        TwinClassFieldEntity fieldEntity = twinClassFieldService.findEntitySafe(fieldId);
        TwinFieldValueSearchNumeric fieldSearch = new TwinFieldValueSearchNumeric()
                .setEquals(value);
        fieldSearch.setTwinClassFieldEntity(fieldEntity);
        ((TwinFieldValueSearch) fieldSearch).setFieldTyper(
                featurerService.getFeaturer(fieldEntity.getFieldTyperFeaturerId(), FieldTyper.class));
        return fieldSearch;
    }
}
