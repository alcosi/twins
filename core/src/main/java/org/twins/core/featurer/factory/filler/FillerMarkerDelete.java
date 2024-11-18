package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsMarkerId;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2319,
        name = "FillerMarkerDelete",
        description = "")
@Slf4j
public class FillerMarkerDelete extends Filler {

    @FeaturerParam(name = "markerId", description = "")
    public static final FeaturerParamUUID markerId = new FeaturerParamUUIDTwinsMarkerId("markerId");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        if (factoryItem.getOutput() instanceof TwinUpdate twinUpdate)
            twinUpdate.deleteMarker(markerId.extract(properties));
    }
}
