package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.filler.FieldLookupMode;

import java.util.Properties;

@Component
@Featurer(id = 2418,
        name = "ConditionerContextValueExists",
        description = "")
@Slf4j
public class ConditionerContextValueExists extends Conditioner {
    @FeaturerParam(name = "twinClassFieldId", description = "")
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUID("twinClassFieldId");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return check(properties, factoryItem, FieldLookupMode.fromContextFields);
    }

    public boolean check(Properties properties, FactoryItem factoryItem, FieldLookupMode fieldLookupMode) throws ServiceException {
        try {
            return null != factoryService.lookupFieldValue(factoryItem, twinClassFieldId.extract(properties), fieldLookupMode);
        } catch (ServiceException e) {
            return false;
        }
    }
}
