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
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapperV2;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Properties;

@Component
@Featurer(id = 2312,
        name = "FillerFieldFromContextField",
        description = "")
@Slf4j
public class FillerFieldFromContextField extends Filler {
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
        FieldValue fieldValue = factoryItem.getFactoryContext().getFields().get(srcTwinClassFieldId.extract(properties));
        if (fieldValue == null) {
            log.warn("TwinClassField[" + srcTwinClassFieldId.extract(properties) + "] is not present in context fields");
            return;
        }
        TwinClassFieldEntity dstTwinClassField = twinClassFieldService.findEntitySafe(dstTwinClassFieldId.extract(properties));
        FieldValue clone = fieldValue.clone();
        clone.setTwinClassField(dstTwinClassField); //value will be copied to dst
        factoryItem.getOutputTwin().addField(clone);
    }
}
