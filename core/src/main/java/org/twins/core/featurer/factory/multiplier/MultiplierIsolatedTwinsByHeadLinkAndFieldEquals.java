package org.twins.core.featurer.factory.multiplier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinFieldClause;
import org.twins.core.domain.TwinFieldFilter;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.domain.search.TwinFieldValueSearchNumeric;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueLinkSingle;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsDataListOptionId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_2215,
        name = "Isolated twins by head, link, field equals and current assignee",
        description = "")
public class MultiplierIsolatedTwinsByHeadLinkAndFieldEquals extends Multiplier {

    @FeaturerParam(name = "Twin class id", description = "Twin class to search", order = 1)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");

    @FeaturerParam(name = "Dst twin class field id", description = "Field to read link dst twin id from context (link field or transition field). If not set, dst twin is read from the context twin forward link", order = 2, optional = true)
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    @FeaturerParam(name = "Dst link id", description = "Link id for search by link dst twin", order = 2)
    public static final FeaturerParamUUID dstLinkId = new FeaturerParamUUIDTwinsLinkId("dstLinkId");

    @FeaturerParam(name = "Equals twin class field id", description = "The ID of the numeric field by which the comparison will be performed.", order = 3)
    public static final FeaturerParamUUID equalsTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("equalsTwinClassFieldId");

    @FeaturerParam(name = "Flavor data list option id", description = "Optional twin flavor for location-specific filtering", order = 8, optional = true)
    public static final FeaturerParamUUID flavorDataListOptionId = new FeaturerParamUUIDTwinsDataListOptionId("flavorDataListOptionId");

    private final TwinSearchService twinSearchService;
    private final TwinClassFieldService twinClassFieldService;
    @Lazy
    @Autowired
    FieldLookupers fieldLookupers;
    @Autowired
    private TwinService twinService;

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        List<FactoryItem> ret = new ArrayList<>();

        UUID dstLink = dstLinkId.extract(properties);
        UUID equalsFieldId = equalsTwinClassFieldId.extract(properties);

        for (FactoryItem inputItem : inputFactoryItemList) {
            TwinEntity contextTwin = inputItem.checkSingleContextTwin();
            UUID headTwinId = resolveHeadTwinId(contextTwin);

            UUID dstTwinId = resolveDstTwinId(properties, inputItem);
            if (dstTwinId == null) {
                log.info("Dst twin is empty for [{}], multiplier step skipped", contextTwin.logShort());
                continue;
            }

            FieldValue equalsFieldValue = fieldLookupers.getFromContextTwinHeadTwinDbFields().lookupFieldValue(inputItem, equalsFieldId);
            if (!(equalsFieldValue instanceof FieldValueText numericField) || numericField.isEmpty()) {
                log.info("Equals field value is empty for [{}], multiplier step skipped", contextTwin.logShort());
                continue;
            }

            double equalsValue;
            try {
                equalsValue = Double.parseDouble(numericField.getValue());
            } catch (NumberFormatException e) {
                throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_VALUE_INCORRECT, "Incorrect numeric value for field [" + equalsFieldId + "]: [" + numericField.getValue() + "]");
            }

            BasicSearch search = new BasicSearch().setCheckViewPermission(false);
            search
                    .addOwnerBusinessAccountId(factoryContext.getRunLimitedByOwnerBusinessAccount())
                    .addTwinClassId(twinClassId.extract(properties), false)
                    .addHeadTwinId(headTwinId)
                    .addLinkDstTwinsId(dstLink, List.of(dstTwinId), false, true)
                    .setFieldsFilter(new TwinFieldFilter()
                            .addClause(new TwinFieldClause()
                                    .addCondition(buildNumericFieldEquals(equalsFieldId, equalsValue))));

            UUID assigneeUserId = contextTwin.getAssignerUserId();
            if (assigneeUserId != null) {
                search.addAssigneeUserId(assigneeUserId, false);
            }

            UUID flavorId = flavorDataListOptionId.extract(properties);
            if (flavorId != null) {
                search.addFlavorDataListOptionId(flavorId, false);
            }

            List<TwinEntity> twins = twinSearchService.findTwins(search);
            for (TwinEntity twin : twins) {
                TwinUpdate twinUpdate = new TwinUpdate();
                twinUpdate
                        .setDbTwinEntity(twin) // original twin
                        .setTwinEntity(twin.clone()); // collecting updated in new twin
                ret.add(new FactoryItem()
                        .setOutput(twinUpdate)
                        .setContextFactoryItemList(List.of(inputItem)));
            }
        }
        return ret;
    }

    protected UUID resolveHeadTwinId(TwinEntity contextTwin) {
        if (contextTwin.getHeadTwinId() != null) {
            TwinEntity headTwin = contextTwin.getHeadTwin();
            if (headTwin == null) {
                headTwin = twinService.findHeadTwin(contextTwin.getHeadTwinId());
            }
            if (headTwin != null && headTwin.getHeadTwinId() != null) {
                return headTwin.getHeadTwinId();
            }
            return null;
        }
        return null;
    }

    private UUID resolveDstTwinId(Properties properties, FactoryItem factoryItem) throws ServiceException {
        UUID dstFieldId = dstTwinClassFieldId.extract(properties);
        if (dstFieldId == null)
            return null;
        FieldValue dstFieldValue = fieldLookupers.getFromContextFieldsAndContextTwinDbFields().lookupFieldValue(factoryItem, dstFieldId);
        return extractTwinIdFromFieldValue(dstFieldValue);
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
        return null;
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
