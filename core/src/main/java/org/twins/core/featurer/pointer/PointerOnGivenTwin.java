package org.twins.core.featurer.pointer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinId;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3106,
        name = "Given twin pointed",
        description = "")
@RequiredArgsConstructor
public class PointerOnGivenTwin extends Pointer {

    @FeaturerParam(name = "Twin id")
    public static final FeaturerParamUUID twinId = new FeaturerParamUUIDTwinsTwinId("twinId");

    private final TwinService twinService;

    @Override
    protected TwinEntity point(Properties properties, TwinEntity srcTwinEntity) throws ServiceException {
        return twinService.findEntitySafe(twinId.extract(properties));
    }
}
