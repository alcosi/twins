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
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsMarkerId;
import org.twins.core.service.twin.TwinMarkerService;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2405,
        name = "TwinMarkerEquals",
        description = "")
@Slf4j
public class ConditionerContextTwinMarkerEquals extends Conditioner {
    @FeaturerParam(name = "Marker id", description = "", order = 1)
    public static final FeaturerParamUUID markerId = new FeaturerParamUUIDTwinsMarkerId("markerId");

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
