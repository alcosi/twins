package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2416,
        name = "Twin of class deep",
        description = "")
@Slf4j
public class ConditionerContextTwinOfClassDeep extends Conditioner {
    @FeaturerParam(name = "Of twin class id", description = "", order = 1)
    public static final FeaturerParamUUID ofTwinClassId = new FeaturerParamUUIDTwinsTwinClassId("ofTwinClassId");

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
        return check(properties, contextItem, recursionCounter - 1);
    }
}
