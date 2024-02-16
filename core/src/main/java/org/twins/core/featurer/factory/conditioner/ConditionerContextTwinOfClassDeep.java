package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;

import java.util.Properties;

@Component
@Featurer(id = 2416,
        name = "ConditionerContextTwinOfClassDeep",
        description = "")
@Slf4j
public class ConditionerContextTwinOfClassDeep extends Conditioner {
    @FeaturerParam(name = "ofTwinClassId", description = "")
    public static final FeaturerParamUUID ofTwinClassId = new FeaturerParamUUID("ofTwinClassId");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return check(properties, factoryItem, 5); //hope 5 is more than enough here
    }

    protected boolean check(Properties properties, FactoryItem factoryItem, int recursionCounter) throws ServiceException {
        if (recursionCounter <= 0)
            return false;
        FactoryItem contextItem = factoryItem.checkNotMultiplyContextItem();
        if (contextItem == null)
            return false;
        if (contextItem.getTwin().getTwinClassId().equals(ofTwinClassId.extract(properties)))
            return true;
        // we will try to look deeper
        return check(properties, factoryItem.checkNotMultiplyContextItem(), recursionCounter - 1);
    }
}
