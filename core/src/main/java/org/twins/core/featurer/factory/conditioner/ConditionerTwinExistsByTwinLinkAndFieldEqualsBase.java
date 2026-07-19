package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinFieldClause;
import org.twins.core.domain.TwinFieldFilter;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.domain.search.TwinFieldValueSearchNumeric;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueLinkSingle;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.*;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinHeadService;
import org.twins.core.service.twin.TwinSearchServiceV2;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Featurer(id = FeaturerTwins.ID_2443,
        name = "Twin exists by head, dst link, twin assignee and field equals",
        description = "True if twin exists with same head, dst link, numeric field value (optional) and dst twin assignee (optional).")
@Slf4j
public class ConditionerTwinExistsByTwinLinkAndFieldEqualsBase extends Conditioner {

    @FeaturerParam(name = "Twin class id", description = "Twin class to search", order = 1)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");

    @FeaturerParam(name = "Dst link id", description = "Link id for search by link dst twin", order = 2)
    public static final FeaturerParamUUID dstLinkId = new FeaturerParamUUIDTwinsLinkId("dstLinkId");

    @FeaturerParam(name = "Head of twin class id", description = "Walk up head hierarchy until twin of this class is found; used as search head", order = 1, optional = true)
    public static final FeaturerParamUUID headTwinClassId = new FeaturerParamUUIDTwinsTwinClassId("headTwinClassId");

    @FeaturerParam(name = "Dst twin class field id", description = "Field to read link dst twin id from context (link field or transition field)", order = 3, optional = true)
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    @FeaturerParam(name = "Equals twin class field id", description = "Numeric field for equals compare. Used both to read the value from context and as a search filter field when equalsSearchTwinClassFieldId is not set", order = 4, optional = true)
    public static final FeaturerParamUUID equalsTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("equalsTwinClassFieldId");

    @FeaturerParam(name = "Equals search twin class field id", description = "Numeric field used in the search filter condition", order = 9, optional = true)
    public static final FeaturerParamUUID equalsSearchTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("equalsSearchTwinClassFieldId");

    @FeaturerParam(name = "Equals field lookupper", description = "Equals field lookupper", order = 6, optional = true, defaultValue = "fromContextFieldsAndContextTwinDbFields")
    public static final FeaturerParamStringTwinsFactoryFieldLookuper equalsFieldLookupper = new FeaturerParamStringTwinsFactoryFieldLookuper("equalsFieldLookupper");

    @FeaturerParam(name = "Match assignee", description = "If true, add dst twin assignerUserId to search when set; ignore assignee when dst twin has none", order = 5, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean matchAssignee = new FeaturerParamBoolean("matchAssignee");

    @FeaturerParam(name = "Exclude factory input twin", description = "Exclude context and factory input twins from search", order = 6, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeFactoryInputTwin = new FeaturerParamBoolean("excludeFactoryInputTwin");

    @FeaturerParam(name = "Match factory item output twin", description = "If true, checks that factory item output twin matches search (for multiplier filter on TwinUpdate)", order = 7, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean matchFactoryItemOutputTwin = new FeaturerParamBoolean("matchFactoryItemOutputTwin");

    @FeaturerParam(name = "Flavor data list option id", description = "Optional twin flavor for location-specific uniqueness", order = 8, optional = true)
    public static final FeaturerParamUUID flavorDataListOptionId = new FeaturerParamUUIDTwinsDataListOptionId("flavorDataListOptionId");

    @Lazy
    @Autowired
    private TwinSearchServiceV2 twinSearchService;
    @Lazy
    @Autowired
    private TwinClassFieldService twinClassFieldService;
    @Lazy
    @Autowired
    private TwinLinkService twinLinkService;
    @Lazy
    @Autowired
    private TwinService twinService;
    @Lazy
    @Autowired
    private TwinHeadService twinHeadService;

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

        UUID headTwinId = twinHeadService.resolveHeadTwinId(contextTwin, headTwinClassId.extract(properties));
        if (headTwinId == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_CONDITION_ERROR, "Head twin not found");
        }

        UUID dstTwinId = resolveDstTwinId(properties, factoryItem, contextTwin);
        if (dstTwinId == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_CONDITION_ERROR, "Dst twin not found");
        }

        BasicSearch search = new BasicSearch().setCheckViewPermission(false);
        search
                .addTwinClassId(twinClassId.extract(properties), false)
                .addHeadTwinId(headTwinId)
                .addLinkDstTwinsId(dstLinkId.extract(properties), List.of(dstTwinId), false, true);

        UUID equalsFieldId = equalsTwinClassFieldId.extract(properties);
        if (equalsFieldId != null) {
            FieldValue equalsFieldValue = resolveEqualsFieldValue(properties, factoryItem, equalsFieldId);
            if (!(equalsFieldValue instanceof FieldValueText priceField) || priceField.isEmpty()) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_CONDITION_ERROR, "Twin field not found");
            }

            UUID searchFieldId = resolveSearchTwinClassFieldId(equalsFieldId, properties);

            double equalsValue;
            try {
                equalsValue = Double.parseDouble(priceField.getValue());
            } catch (NumberFormatException e) {
                throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_VALUE_INCORRECT,
                        "Incorrect numeric value for field [" + equalsFieldId + "]: [" + priceField.getValue() + "]");
            }

            search.setFieldsFilter(new TwinFieldFilter()
                    .addClause(new TwinFieldClause()
                            .addCondition(buildNumericFieldEquals(searchFieldId, equalsValue))));

        }

        if (matchAssignee.extract(properties)) {
            UUID assigneeUserId = resolveAssigneeUserIdFromDstTwin(dstTwinId);
            if (assigneeUserId != null) {
                search.addAssigneeUserId(assigneeUserId, false);
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
            return contextTwin.getTwinLinks().getForwardLinks().getGrouped(linkId).getFirst().getDstTwin().getId();
        } catch (Exception e) {
            log.info("Link dst twin not found by link [{}] on context twin [{}]", linkId, contextTwin.logShort());
            return null;
        }
    }

    private UUID resolveSearchTwinClassFieldId(UUID equalsFieldId, Properties properties) {
        UUID searchFieldId = equalsSearchTwinClassFieldId.extract(properties);
        if (searchFieldId == null) {
            searchFieldId = equalsFieldId;
        }
        return searchFieldId;
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

    private UUID resolveAssigneeUserIdFromDstTwin(UUID dstTwinId) throws ServiceException {
        return twinService.findEntitySafe(dstTwinId).getAssignerUserId();
    }

    private FieldValue resolveEqualsFieldValue(Properties properties, FactoryItem inputItem, UUID equalsFieldId) throws ServiceException {
        return ((FieldLookuperNearest) fieldLookupers.getByType(equalsFieldLookupper.extract(properties)))
                .lookupFieldValue(inputItem, equalsFieldId);
    }

    private TwinFieldSearch buildNumericFieldEquals(UUID fieldId, double value) throws ServiceException {
        TwinClassFieldEntity fieldEntity = twinClassFieldService.findEntitySafe(fieldId);
        TwinFieldValueSearchNumeric fieldSearch = new TwinFieldValueSearchNumeric()
                .setEquals(value);
        fieldSearch.setTwinClassFieldEntity(fieldEntity);
        fieldSearch.setFieldTyper(
                featurerService.getFeaturer(fieldEntity.getFieldTyperFeaturerId(), FieldTyper.class));
        return fieldSearch;
    }
}
