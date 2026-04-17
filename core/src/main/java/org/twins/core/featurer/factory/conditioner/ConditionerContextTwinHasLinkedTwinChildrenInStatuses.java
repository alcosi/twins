package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2442,
        name = "Context twin has linked twin children in statuses",
        description = "Check factory item twin has a linked" +
                " twin (by configured backward link) that has child twins" +
                " of in one of the configured statuses.")
@Slf4j
public class ConditionerContextTwinHasLinkedTwinChildrenInStatuses extends Conditioner {
    @FeaturerParam(name = "Link id", description = "", order = 1)
    public static final FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @FeaturerParam(name = "Status ids", description = "", order = 2)
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @Lazy
    @Autowired
    private TwinRepository twinRepository;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        if (factoryItem.getTwin() == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory item twin is empty, cannot check linked twins children in statuses");
        }

        return twinRepository.existsChildrenByBackwardLinkAndStatuses(
                factoryItem.getTwin().getId(),
                linkId.extract(properties),
                statusIds.extract(properties)
        );
    }
}
