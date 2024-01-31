package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.filler.Filler;

import java.util.Properties;

@Component
@Featurer(id = 2412,
        name = "ConditionerContextTwinOfClass",
        description = "")
@Slf4j
public class ConditionerContextTwinOfClass extends Conditioner {
    @FeaturerParam(name = "ofTwinClassId", description = "")
    public static final FeaturerParamUUID ofTwinClassId = new FeaturerParamUUID("ofTwinClassId");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        TwinEntity contextTwin = Filler.checkNotMultiplyContextTwin(factoryItem);
        if (contextTwin == null)
            return false;
        return contextTwin.getTwinClassId().equals(ofTwinClassId.extract(properties));
    }
}
