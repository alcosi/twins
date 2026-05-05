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
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.service.link.TwinLinkService;

import java.util.Collection;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1606,
        name = "Twin has link",
        description = "")
public class TwinValidatorTwinHasLink extends TwinValidator {
    @FeaturerParam(name = "Link id", description = "", order = 1)
    public static final FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @Lazy
    @Autowired
    TwinLinkService twinLinkService;

    @Override
    protected CollectionValidationResult isValid(Properties properties, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException {
        var linkIdUUID = linkId.extract(properties);
        twinLinkService.loadTwinLinks(twinEntityCollection);
        var result = new CollectionValidationResult();
        for (var twinEntity : twinEntityCollection) {
            boolean isValid = twinLinkService.hasLink(twinEntity, linkIdUUID);
            result.getTwinsResults().put(twinEntity.getId(), buildResult(
                    isValid,
                    invert,
                    twinEntity.logShort() + " has no link[" + linkIdUUID + "]",
                    twinEntity.logShort() + " has some link[" + linkIdUUID + "]"));
        }
        return result;
    }
}
