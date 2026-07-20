package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperLink;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueLinkSingle;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(
        id = FeaturerTwins.ID_2360,
        name = "Forward link to twin found by head and context field link dst",
        description = "Finds twin by head and link dst resolved from context field; creates forward link from output twin."
)
@Slf4j
public class FillerForwardLinkToTwinFoundByHeadAndContextFieldLinkDst extends FillerForwardLinkToTwinFoundByHeadAndLinkDstBase {

    @FeaturerParam(name = "Dst twin class field id", description = "Field to read link dst twin id from context (link field or transition field)", order = 3)
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    @Lazy
    @Autowired
    private TwinClassFieldService twinClassFieldService;

    @Override
    protected UUID getLinkId(Properties properties) throws ServiceException {
        UUID dstFieldId = dstTwinClassFieldId.extract(properties);
        return twinClassFieldService.getConfiguredLinkSafe(dstFieldId);
    }

    @Override
    protected UUID resolveDstTwinId(Properties properties, FactoryItem factoryItem, TwinEntity contextTwin) throws ServiceException {
        UUID dstFieldId = dstTwinClassFieldId.extract(properties);
        FieldValue dstFieldValue = fieldLookupers.getFromContextFieldsAndContextTwinDbFields()
                .lookupFieldValue(factoryItem, dstFieldId);
        return extractTwinIdFromFieldValue(dstFieldValue);
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

