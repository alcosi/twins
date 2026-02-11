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
import org.twins.core.service.twin.TwinService;

import java.util.Properties;
import java.util.UUID;


@Component
@Featurer(id = FeaturerTwins.ID_2334,
        name = "Field as context field head",
        description = "Get head for twin from src field(link). Set this head to dst field(link)")
@Slf4j
public class FillerFieldAsContextFieldHead extends Filler {

    @FeaturerParam(name = "Src twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID srcTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("srcTwinClassFieldId");

    @FeaturerParam(name = "Dst twin class field id", description = "", order = 2)
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    @Lazy
    @Autowired
    TwinService twinService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        UUID extractedSrcTwinClassFieldId = srcTwinClassFieldId.extract(properties);
        FieldValue srcFieldValue = fieldLookupers.getFromContextFields().lookupFieldValue(factoryItem, extractedSrcTwinClassFieldId);

        UUID detectedHeadId = null;
        if (srcFieldValue instanceof FieldValueLink fieldValueLink) {
            if(fieldValueLink.isEmpty())
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "srcTwinClassField[" + extractedSrcTwinClassFieldId + "] is not filled");
            TwinEntity dstTwin = fieldValueLink.getItems().getFirst().getDstTwin();
            detectedHeadId = dstTwin.getHeadTwinId();
            if(null == detectedHeadId)
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No head twin detected for twin: " + dstTwin.logDetailed());
        } else
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "srcTwinClassField[" + extractedSrcTwinClassFieldId + "] is not instance of link field");

        FieldValue dstFieldValue = twinService.createFieldValue(dstTwinClassFieldId.extract(properties), String.valueOf(detectedHeadId));
        factoryItem.getOutput().addField(dstFieldValue);
    }
}
