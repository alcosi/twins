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
@Featurer(id = FeaturerTwins.ID_2419,
        name = "FactoryContextTwinOfClassDeep",
        description = "")
@Slf4j
public class ConditionerFactoryContextTwinOfClassDeep extends Conditioner {
    @FeaturerParam(name = "Of twin class id", description = "", order = 1)
    public static final FeaturerParamUUID ofTwinClassId = new FeaturerParamUUIDTwinsTwinClassId("ofTwinClassId");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        for (var fi : factoryItem.getFactoryContext().getFactoryItemList()) {
            if (check(properties, fi, 5))
                return true;
        }
        return false;
    }

    protected boolean check(Properties properties, FactoryItem factoryItem, int recursionCounter) throws ServiceException {
        if (recursionCounter <= 0)
            return false;
        if (factoryItem.getTwin().getTwinClassId().equals(ofTwinClassId.extract(properties)))
            return true;
        FactoryItem contextItem = factoryItem.checkNotMultiplyContextItem();
        if (contextItem == null)
            return false;
        // we will try to look deeper
        return check(properties, contextItem, recursionCounter - 1);
    }
}
