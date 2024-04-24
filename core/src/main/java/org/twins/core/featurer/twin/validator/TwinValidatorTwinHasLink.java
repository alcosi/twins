package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.link.TwinLinkService;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = 1606,
        name = "TwinValidatorTwinHasLink",
        description = "")
public class TwinValidatorTwinHasLink extends TwinValidator {
    @FeaturerParam(name = "linkId", description = "")
    public static final FeaturerParamUUID linkId = new FeaturerParamUUID("linkId");

    @Lazy
    @Autowired
    TwinLinkService twinLinkService;

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        boolean isValid = twinLinkService.hasLink(twinEntity, linkId.extract(properties));
        return buildResult(
                isValid,
                invert,
                twinEntity.logShort() + " has no link[" + linkId.extract(properties) + "]",
                twinEntity.logShort() + " has some link[" + linkId.extract(properties) + "]");
    }
}
