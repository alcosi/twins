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
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapperV2;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Properties;

@Component
@Featurer(id = 2311,
        name = "FillerFieldFromContextTwinField",
        description = "")
@Slf4j
public class FillerFieldFromContextTwinField extends Filler {
    @FeaturerParam(name = "srcTwinClassFieldId", description = "")
    public static final FeaturerParamUUID srcTwinClassFieldId = new FeaturerParamUUID("srcTwinClassFieldId");

    @FeaturerParam(name = "dstTwinClassFieldId", description = "")
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUID("dstTwinClassFieldId");
    @Lazy
    @Autowired
    TwinClassService twinClassService;

    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    TwinClassFieldService twinClassFieldService;


    @Lazy
    @Autowired
    TwinFieldRestDTOMapperV2 twinFieldRestDTOMapperV2;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity contextTwin = checkSingleContextTwin(factoryItem);
        // we have to check missing fields because of links (link can be added to twin not by fields mechanism)
        TwinFieldEntity srcField = twinService.findTwinFieldIncludeMissing(contextTwin.getId(), srcTwinClassFieldId.extract(properties));
        if (srcField == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "twinClassField[" + srcTwinClassFieldId.extract(properties) + "] is not present for context " + contextTwin.logShort());
        TwinClassFieldEntity dstTwinClassField = twinClassFieldService.findEntitySafe(dstTwinClassFieldId.extract(properties));
        FieldValue fieldValue = twinService.getTwinFieldValue(srcField);
        FieldValue clone = fieldValue.clone();
        clone.setTwinClassField(dstTwinClassField);
        factoryItem.getOutputTwin().addField(clone);
    }
}
