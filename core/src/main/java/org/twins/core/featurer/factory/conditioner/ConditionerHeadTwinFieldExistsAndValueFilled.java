package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2426,
        name = "Head twin field exists and value filled",
        description = "Check head twin has field and its value not empty")
@Slf4j
public class ConditionerHeadTwinFieldExistsAndValueFilled extends Conditioner {

    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        FieldValue fieldValue = null;
        try {
            fieldValue = fieldLookupers.getFromContextTwinHeadTwinDbFields().lookupFieldValue(factoryItem, twinClassFieldId.extract(properties));
        } catch (ServiceException e) {
           return false;
        }
        return fieldValue != null && fieldValue.isNotEmpty();
    }
}
