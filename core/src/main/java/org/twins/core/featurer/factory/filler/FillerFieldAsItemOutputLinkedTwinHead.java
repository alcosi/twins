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
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2366,
        name = "Field as item output linked twin head",
        description = "")
@Slf4j
public class FillerFieldAsItemOutputLinkedTwinHead extends Filler {
    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    TwinLinkService twinLinkService;

    @FeaturerParam(name = "linkedTwinByTwinClassFieldId", description = "")
    public static final FeaturerParamUUID linkedTwinByTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("linkedTwinByTwinClassFieldId");

    @FeaturerParam(name = "dstTwinClassFieldId", description = "")
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        var srcTwinClassFieldId = linkedTwinByTwinClassFieldId.extract(properties);
        FieldValue fieldValue = fieldLookupers.getFromItemOutputFields().lookupFieldValue(factoryItem, srcTwinClassFieldId);
        if (fieldValue instanceof FieldValueLink itemOutputFieldLink) {
            if (itemOutputFieldLink.isEmpty()) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "{} does not contain a links", factoryItem.getTwin());
            }
            if (itemOutputFieldLink.getItems().size() != 1) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "{} contains more than one link in field[{}]", factoryItem.getTwin(), srcTwinClassFieldId );
            }
            var link = itemOutputFieldLink.getItems().getFirst();
            twinLinkService.loadDstTwin(link);
            var headTwinId = link.getDstTwin().getHeadTwinId();
            if (headTwinId == null) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "{} does not contain a head twin", link.getDstTwin().logShort());
            }
            var dstFieldValue = twinService.createFieldValue(dstTwinClassFieldId.extract(properties), String.valueOf(headTwinId));
            factoryItem.getOutput().addField(dstFieldValue);
        } else {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Field is not of type link");
        }
    }
}
