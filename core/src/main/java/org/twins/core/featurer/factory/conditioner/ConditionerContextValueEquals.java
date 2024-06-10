package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.filler.FieldLookupMode;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2415,
        name = "ConditionerContextValueEquals",
        description = "")
@Slf4j
public class ConditionerContextValueEquals extends Conditioner {
    @FeaturerParam(name = "twinClassFieldId", description = "")
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "value", description = "")
    public static final FeaturerParamString value = new FeaturerParamString("value");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return check(properties, factoryItem, FieldLookupMode.fromContextFieldsAndContextTwinFields);
    }

    public boolean check(Properties properties, FactoryItem factoryItem, FieldLookupMode fieldLookupMode) throws ServiceException {
        FieldValue fieldValue = factoryService.lookupFieldValue(factoryItem, twinClassFieldId.extract(properties), fieldLookupMode);
        return fieldValue.hasValue(value.extract(properties));
    }
}
