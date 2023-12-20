package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapperV2;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Properties;

@Component
@Featurer(id = 2402,
        name = "ConditionerContextFieldValueEquals",
        description = "")
@Slf4j
public class ConditionerContextFieldValueEquals extends Conditioner {
    @FeaturerParam(name = "twinClassFieldId", description = "")
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUID("twinClassFieldId");

    @FeaturerParam(name = "value", description = "")
    public static final FeaturerParamString value = new FeaturerParamString("value");

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
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        FieldValue fieldValue = factoryItem.getFactoryContext().getFields().get(twinClassFieldId.extract(properties));
        if (fieldValue == null) {
            log.warn("twinClassField[" + twinClassFieldId.extract(properties) + "] is not present in context fields");
            return false;
        }
        return fieldValue.hasValue(value.extract(properties));
    }
}
