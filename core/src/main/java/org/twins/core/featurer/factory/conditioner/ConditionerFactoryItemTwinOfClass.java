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
@Featurer(id = FeaturerTwins.ID_2414,
        name = "Factory item twin of class",
        description = "")
@Slf4j
public class ConditionerFactoryItemTwinOfClass extends Conditioner {
    @FeaturerParam(name = "Of twin class id", description = "")
    public static final FeaturerParamUUID ofTwinClassId = new FeaturerParamUUIDTwinsTwinClassId("ofTwinClassId");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return factoryItem.getOutput().getTwinEntity().getTwinClassId().equals(ofTwinClassId.extract(properties));
    }
}
