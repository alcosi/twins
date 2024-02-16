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
import org.twins.core.service.twin.TwinMarkerService;

import java.util.Properties;

@Component
@Featurer(id = 2405,
        name = "ConditionerContextTwinMarkerEquals",
        description = "")
@Slf4j
public class ConditionerContextTwinMarkerEquals extends Conditioner {
    @FeaturerParam(name = "markerId", description = "")
    public static final FeaturerParamUUID markerId = new FeaturerParamUUID("markerId");

    @Lazy
    @Autowired
    TwinMarkerService twinMarkerService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        TwinEntity contextTwin = factoryItem.checkNotMultiplyContextTwin();
        if (contextTwin == null)
            return false;
        return twinMarkerService.hasMarker(contextTwin, markerId.extract(properties));
    }
}
