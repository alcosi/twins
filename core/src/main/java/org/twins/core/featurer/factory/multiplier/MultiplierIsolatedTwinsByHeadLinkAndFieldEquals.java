package org.twins.core.featurer.factory.multiplier;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
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
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.domain.search.TwinFieldValueSearchNumeric;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueLinkSingle;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.*;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2215,
        name = "Isolated twins by head, link, field equals and current assignee",
        description = "")
public class MultiplierIsolatedTwinsByHeadLinkAndFieldEquals extends Multiplier {

    @FeaturerParam(name = "Twin class id", description = "Twin class to search", order = 1)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");

    @FeaturerParam(name = "Head of twin class id", description = "Walk up head hierarchy until twin of this class is found; used as search head", order = 1)
    public static final FeaturerParamUUID headTwinClassId = new FeaturerParamUUIDTwinsTwinClassId("headTwinClassId");

    @FeaturerParam(name = "Dst twin class field id", description = "Field to read link dst twin id from context (link field or transition field). If not set, dst twin is read from the context twin forward link", order = 2, optional = true)
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    @FeaturerParam(name = "Dst field lookupper", description = "Dst field lookupper", order = 6, optional = true)
    public static final FeaturerParamStringTwinsFactoryFieldLookuper dstFieldLookupper = new FeaturerParamStringTwinsFactoryFieldLookuper("dstFieldLookupper");

    @FeaturerParam(name = "Dst link id", description = "Link id for search by link dst twin", order = 2)
    public static final FeaturerParamUUID dstLinkId = new FeaturerParamUUIDTwinsLinkId("dstLinkId");

    @FeaturerParam(name = "Equals twin class field id", description = "The ID of the numeric field by which the comparison will be performed.", order = 3)
    public static final FeaturerParamUUID equalsTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("equalsTwinClassFieldId");

    @FeaturerParam(name = "Equals field lookupper", description = "Equals field lookupper", order = 6, optional = true)
    public static final FeaturerParamStringTwinsFactoryFieldLookuper equalsFieldLookupper = new FeaturerParamStringTwinsFactoryFieldLookuper("equalsFieldLookupper");

    @FeaturerParam(name = "Match assignee", description = "If true, add link dst twin assigneeUserId to search when set; ignore assignee when dst twin has none", order = 4, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean matchAssignee = new FeaturerParamBoolean("matchAssignee");

    @FeaturerParam(name = "Flavor data list option id", description = "Optional twin flavor for location-specific filtering", order = 8, optional = true)
    public static final FeaturerParamUUID flavorDataListOptionId = new FeaturerParamUUIDTwinsDataListOptionId("flavorDataListOptionId");

    @Lazy
    @Autowired
    private TwinClassFieldService twinClassFieldService;
    @Lazy
    @Autowired
    private FieldLookupers fieldLookupers;
    @Autowired
    private TwinService twinService;
    @Autowired
    private TwinSearchService twinSearchService;

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        List<FactoryItem> ret = new ArrayList<>();

        UUID dstLink = dstLinkId.extract(properties);
        UUID equalsFieldId = equalsTwinClassFieldId.extract(properties);

        for (FactoryItem inputItem : inputFactoryItemList) {
            TwinEntity contextTwin = inputItem.checkSingleContextTwin();
            UUID headTwinId = resolveHeadTwinId(contextTwin, headTwinClassId.extract(properties));

            UUID dstTwinId = resolveDstTwinId(properties, inputItem);
            if (dstTwinId == null) {
                log.info("Dst twin is empty for [{}], multiplier step skipped", contextTwin.logShort());
                continue;
            }

            FieldValue equalsFieldValue = resolveEqualsFieldValue(properties, inputItem, equalsFieldId);
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

            if (matchAssignee.extract(properties)) {
                UUID assigneeUserId = twinService.findEntitySafe(dstTwinId).getAssignerUserId();
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

    private FieldValue resolveEqualsFieldValue(Properties properties, FactoryItem inputItem, UUID equalsFieldId) throws ServiceException {
        return ((FieldLookuperNearest) fieldLookupers.getByType(equalsFieldLookupper.extract(properties)))
                .lookupFieldValue(inputItem, equalsFieldId);
    }

    protected UUID resolveHeadTwinId(TwinEntity contextTwin, UUID headTwinClassId) throws ServiceException {
        if (headTwinClassId == null) {
            return contextTwin.getHeadTwinId() != null ? contextTwin.getHeadTwinId() : contextTwin.getId();
        }
        TwinEntity current = contextTwin;
        for (int depth = 0; depth < 10; depth++) {
            if (current.getHeadTwinId() == null) {
                return null;
            }
            if (headTwinClassId.equals(current.getTwinClassId())) {
                return current.getHeadTwinId();
            }
            TwinEntity head = current.getHeadTwin() != null ? current.getHeadTwin() : twinService.loadHead(current);
            if (head == null) {
                return null;
            }
            current = head;
        }
        return null;
    }

    private UUID resolveDstTwinId(Properties properties, FactoryItem factoryItem) throws ServiceException {
        UUID dstFieldId = dstTwinClassFieldId.extract(properties);
        if (dstFieldId == null)
            return null;
        FieldValue dstFieldValue = ((FieldLookuperNearest) fieldLookupers.getByType(dstFieldLookupper.extract(properties)))
                .lookupFieldValue(factoryItem, dstFieldId);
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
