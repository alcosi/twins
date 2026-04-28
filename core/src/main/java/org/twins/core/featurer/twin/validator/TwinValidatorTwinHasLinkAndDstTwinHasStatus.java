package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.service.link.TwinLinkService;

import java.util.Collection;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1616,
        name = "Twin has link and dst twin has status",
        description = "")
public class TwinValidatorTwinHasLinkAndDstTwinHasStatus extends TwinValidator {

    @FeaturerParam(name = "Link id", description = "", order = 1)
    public static final FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @FeaturerParam(name = "Twin statuses", description = "", order = 2)
    public static final FeaturerParamUUIDSet twinStatusIds = new FeaturerParamUUIDSetTwinsStatusId("twinStatusIds");

    @Lazy
    @Autowired
    TwinLinkService twinLinkService;

    @Override
    protected CollectionValidationResult isValid(Properties properties, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException {
        var linkIdUUID = linkId.extract(properties);
        var statusIds = twinStatusIds.extract(properties);
        twinLinkService.loadTwinLinks(twinEntityCollection);
        var result = new CollectionValidationResult();
        for (var twinEntity : twinEntityCollection) {
            boolean isValid = twinLinkService.hasLink(twinEntity, linkIdUUID) && twinLinkService.isLinkDstTwinStatusIn(twinEntity, linkIdUUID, statusIds);
            result.getTwinsResults().put(twinEntity.getId(), buildResult(
                    isValid,
                    invert,
                    twinEntity.logShort() + " has no link[" + linkIdUUID + "] or dst twin status not in " + statusIds,
                    twinEntity.logShort() + " has some link[" + linkIdUUID + "] and dst twin status in " + statusIds));
        }
        return result;
    }
}
