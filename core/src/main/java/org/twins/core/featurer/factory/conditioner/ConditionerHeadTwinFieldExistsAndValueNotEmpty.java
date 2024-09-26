package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.filler.FieldLookupMode;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2426,
        name = "ConditionerHeadTwinFieldExistsAndValueNotEmpty",
        description = "")
@Slf4j
public class ConditionerHeadTwinFieldExistsAndValueNotEmpty extends ConditionerContextValueEquals {

    @FeaturerParam(name = "twinClassFieldId", description = "")
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        FieldValue fieldValue = null;
        try {
            fieldValue = factoryService.lookupFieldValue(factoryItem, twinClassFieldId.extract(properties), FieldLookupMode.fromContextTwinHeadTwinFields);
        } catch (ServiceException e) {
           return false;
        }
        return fieldValue != null && !fieldValue.hasValue("") && !fieldValue.hasValue(null);
    }
}
