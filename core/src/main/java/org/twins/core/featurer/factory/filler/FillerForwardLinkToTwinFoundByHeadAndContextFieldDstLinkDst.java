package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueLinkSingle;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamStringTwinsFactoryFieldLookuper;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(
        id = FeaturerTwins.ID_2359,
        name = "Forward link to twin found by head and field dst",
        description = "Finds twin by head and optional link dst; dst twin id from field. " +
                "Search link id from optional dst link id param. Creates forward link from twin."
)
@Slf4j
public class FillerForwardLinkToTwinFoundByHeadAndContextFieldDstLinkDst extends FillerForwardLinkToTwinFoundByHeadAndLinkDstBase {

    @FeaturerParam(name = "Dst link id", description = "Link id for search by link dst twin", order = 5)
    public static final FeaturerParamUUID dstLinkId = new FeaturerParamUUIDTwinsLinkId("dstLinkId");

    @FeaturerParam(name = "Dst field lookupper", description = "Dst field lookupper", order = 6, optional = true)
    public static final FeaturerParamStringTwinsFactoryFieldLookuper dstFieldLookupper = new FeaturerParamStringTwinsFactoryFieldLookuper("dstFieldLookupper");

    @FeaturerParam(name = "Dst twin class field id", description = "Field to read link dst twin id from context (link field or transition field)", order = 7)
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    @Override
    protected UUID resolveDstTwinId(Properties properties, FactoryItem factoryItem, TwinEntity contextTwin) throws ServiceException {
        UUID dstFieldId = dstTwinClassFieldId.extract(properties);
        FieldValue dstFieldValue = ((FieldLookuperNearest) fieldLookupers.getByType(dstFieldLookupper.extract(properties)))
                .lookupFieldValue(factoryItem, dstFieldId);
        return extractTwinIdFromFieldValue(dstFieldValue);
    }

    @Override
    protected UUID getLinkId(Properties properties) throws ServiceException {
        return dstLinkId.extract(properties);
    }

    private UUID extractTwinIdFromFieldValue(FieldValue fieldValue) throws ServiceException {
        if (fieldValue instanceof FieldValueLinkSingle linkSingle && linkSingle.isNotEmpty()) {
            return linkSingle.getValue() != null ? linkSingle.getValue().getId() : null;
        }
        if (fieldValue instanceof FieldValueLink link && link.isNotEmpty()) {
            var linkEntity = link.getItems().getFirst();
            if (linkEntity.getDstTwin() != null) {
                return linkEntity.getDstTwin().getId();
            }
            return linkEntity.getDstTwinId();
        }
        if (fieldValue instanceof FieldValueText) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR,
                    "dstTwinClassFieldId is expected to be a link field, but it's FieldValueText.");
        }
        return null;
    }
}
