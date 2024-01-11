package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.domain.factory.FactoryItem;

import java.util.Properties;

@Component
@Featurer(id = 2319,
        name = "FillerMarkerDelete",
        description = "")
@Slf4j
public class FillerMarkerDelete extends Filler {

    @FeaturerParam(name = "markerId", description = "")
    public static final FeaturerParamUUID markerId = new FeaturerParamUUID("markerId");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        if (factoryItem.getOutputTwin() instanceof TwinUpdate twinUpdate)
            twinUpdate.deleteMarker(markerId.extract(properties));
    }
}
