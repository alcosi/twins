package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2341,
        name = "Field from featurer params",
        description = "")
@Slf4j
public class FillerFieldFromParams extends Filler {
    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "Value", description = "", order = 2)
    public static final FeaturerParamString value = new FeaturerParamString("value");

    @Lazy
    @Autowired
    private TwinService twinService;


    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        UUID extractedTwinClassFieldId = twinClassFieldId.extract(properties);
        FieldValue fieldValue = twinService.getTwinFieldValue(twinService.wrapField(factoryItem.getOutput().getTwinEntity(), extractedTwinClassFieldId));
        if (!(fieldValue instanceof FieldValueText)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "twinClassField[" + extractedTwinClassFieldId + "] is not instance of text field");
        }
        fieldValue = new FieldValueText(fieldValue.getTwinClassField()).setValue(value.extract(properties));
        factoryItem.getOutput().addField(fieldValue);
    }
}
