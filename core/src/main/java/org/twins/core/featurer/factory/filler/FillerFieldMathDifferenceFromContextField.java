package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.domain.TwinCreate;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapperV2;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = 2321,
        name = "FillerFieldMathDifferenceFromContextField",
        description = "")
@Slf4j
public class FillerFieldMathDifferenceFromContextField extends Filler {
    @FeaturerParam(name = "minuendTwinClassFieldId", description = "")
    public static final FeaturerParamUUID minuendTwinClassFieldId = new FeaturerParamUUID("minuendTwinClassFieldId");
    @FeaturerParam(name = "subtrahendTwinClassFieldId", description = "Value from this field will be ")
    public static final FeaturerParamUUID subtrahendTwinClassFieldId = new FeaturerParamUUID("subtrahendTwinClassFieldId");
    @FeaturerParam(name = "allowNegativeResult", description = "")
    public static final FeaturerParamBoolean allowNegativeResult = new FeaturerParamBoolean("allowNegativeResult");
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
        UUID paramSubtrahendTwinClassFieldId = subtrahendTwinClassFieldId.extract(properties);
        UUID paramMinuendTwinClassFieldId = minuendTwinClassFieldId.extract(properties);
        FieldValue subtrahendFieldValue = factoryItem.getFactoryContext().getFields().get(paramSubtrahendTwinClassFieldId);
        if (subtrahendFieldValue == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "subtrahendTwinClassField[" + paramSubtrahendTwinClassFieldId + "] is not present in context fields");
        if (!(subtrahendFieldValue instanceof FieldValueText))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "subtrahendTwinClassField[" + paramSubtrahendTwinClassFieldId + "] is not instance of text field and can not be converted to number");
        Number subtrahendNumber = NumberUtils.createNumber(((FieldValueText) subtrahendFieldValue).getValue());

        FieldValue minuendFieldValue = factoryItem.getOutputTwin().getField(paramMinuendTwinClassFieldId);
        if (minuendFieldValue == null && factoryItem.getOutputTwin() instanceof TwinCreate)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory item is under creation and has no created field[" + paramMinuendTwinClassFieldId + "]");
        if (factoryItem.getOutputTwin() instanceof TwinUpdate) { //perhaps we can get field value from database
            TwinFieldEntity twinFieldEntity = twinService.findTwinField(factoryItem.getOutputTwin().getTwinEntity().getId(), paramMinuendTwinClassFieldId);
            if (twinFieldEntity == null)
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "minuendTwinClassField[" + paramMinuendTwinClassFieldId + "] is not present in " + factoryItem.getOutputTwin().getTwinEntity().getId());
            minuendFieldValue = twinService.getTwinFieldValue(twinFieldEntity);
        }
        if (minuendFieldValue == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "minuendTwinClassField[" + paramMinuendTwinClassFieldId + "] can not be detected");
        if (minuendFieldValue instanceof FieldValueText minuedFieldValueText) {
            Number minuendNumber = NumberUtils.createNumber(minuedFieldValueText.getValue());
            double difference = minuendNumber.doubleValue() - subtrahendNumber.doubleValue();
            if (!allowNegativeResult.extract(properties) && difference < 0)
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "negative difference result");
            factoryItem.getOutputTwin().addField(minuedFieldValueText.setValue(fmt(difference)));
        } else
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "minuendTwinClassField[" + paramMinuendTwinClassFieldId + "] is not instance of text field and can not be converted to number");

    }

    public static String fmt(double d)
    {
        if(d == (long) d)
            return String.format("%d",(long)d);
        else
            return String.format("%s",d);
    }
}
