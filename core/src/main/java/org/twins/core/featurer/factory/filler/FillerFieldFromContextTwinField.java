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
        TwinEntity outputTwinEntity = factoryItem.getOutputTwin().getTwinEntity();
        TwinEntity contextTwin = checkNotMultiplyContextTwin(factoryItem);
        TwinFieldEntity srcField = twinService.findTwinField(contextTwin.getId(), srcTwinClassFieldId.extract(properties));
        if (srcField != null) {
            log.warn("twinClassField[" + srcTwinClassFieldId.extract(properties) + "] is not present for context " + contextTwin.logShort());
            return;
        }
        TwinClassFieldEntity dstTwinClassField = twinClassFieldService.findEntitySafe(dstTwinClassFieldId.extract(properties));
        FieldValue fieldValue = null;
        try {
            fieldValue = twinFieldRestDTOMapperV2.convert(srcField);
            log.info(outputTwinEntity.logShort() + " " + dstTwinClassField.logShort() + " will be filled from context twin " + srcField);
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_VALUE_INCORRECT);
        }
        fieldValue.setTwinClassField(dstTwinClassField); //value will be copied to dst
        factoryItem.getOutputTwin().addField(fieldValue);
    }
}
