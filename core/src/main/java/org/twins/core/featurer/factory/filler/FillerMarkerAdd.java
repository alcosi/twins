package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsMarkerId;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2318,
        name = "MarkerAdd",
        description = "")
@Slf4j
public class FillerMarkerAdd extends Filler {

    @FeaturerParam(name = "Marker id", description = "", order = 1)
    public static final FeaturerParamUUID markerId = new FeaturerParamUUIDTwinsMarkerId("markerId");

    @FeaturerParam(name = "hardAdd", description = "", order = 2)
    public static final FeaturerParamBoolean hardAdd = new FeaturerParamBoolean("hardAdd");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        factoryItem.getOutput().addMarker(markerId.extract(properties));
        if (factoryItem.getOutput() instanceof TwinUpdate twinUpdate)
            if (hardAdd.extract(properties) && null != twinUpdate.getMarkersDelete())
                twinUpdate.getMarkersDelete().remove(markerId.extract(properties));
    }
}
