package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsMarkerId;
import org.twins.core.service.twin.TwinMarkerService;

import java.util.Collection;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1603,
        name = "Twin marker exist",
        description = "")
public class TwinValidatorTwinMarkerExist extends TwinValidator {
    @FeaturerParam(name = "Marker id", description = "", order = 1)
    public static final FeaturerParamUUID markerId = new FeaturerParamUUIDTwinsMarkerId("markerId");

    @Lazy
    @Autowired
    TwinMarkerService twinMarkerService;
    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        boolean isValid = twinMarkerService.hasMarker(twinEntity, markerId.extract(properties));
        return buildResult(
                isValid,
                invert,
                twinEntity.logShort() + " does not have marker[" + markerId.extract(properties) + "]",
                twinEntity.logShort() + " has marker[" + markerId.extract(properties) + "]");
    }

    @Override
    protected CollectionValidationResult isValid(Properties properties, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException {
        twinMarkerService.loadMarkers(twinEntityCollection); // group loading will reduce db query count
        return super.isValid(properties, twinEntityCollection, invert);
    }
}
