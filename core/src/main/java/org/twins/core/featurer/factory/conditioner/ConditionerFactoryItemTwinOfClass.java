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
@Featurer(id = 2414,
        name = "ConditionerFactoryItemTwinOfClass",
        description = "")
@Slf4j
public class ConditionerFactoryItemTwinOfClass extends Conditioner {
    @FeaturerParam(name = "ofTwinClassId", description = "")
    public static final FeaturerParamUUID ofTwinClassId = new FeaturerParamUUID("ofTwinClassId");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return factoryItem.getOutputTwin().getTwinEntity().getTwinClassId().equals(ofTwinClassId.extract(properties));
    }
}
