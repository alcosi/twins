package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.filler.Filler;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Properties;

@Component
@Featurer(id = 2411,
        name = "ConditionerContextTwinInstanceOf",
        description = "")
@Slf4j
public class ConditionerContextTwinInstanceOf extends Conditioner {
    @FeaturerParam(name = "instanceOfTwinClassId", description = "")
    public static final FeaturerParamUUID instanceOfTwinClassId = new FeaturerParamUUID("instanceOfTwinClassId");

    @Lazy
    @Autowired
    TwinClassService twinClassService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        TwinEntity contextTwin = Filler.checkNotMultiplyContextTwin(factoryItem);
        if (contextTwin == null)
            return false;
        return twinClassService.isInstanceOf(contextTwin.getTwinClassId(), instanceOfTwinClassId.extract(properties));
    }
}
