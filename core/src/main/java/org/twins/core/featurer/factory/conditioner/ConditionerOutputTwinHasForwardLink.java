package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.service.link.TwinLinkService;

import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2448,
        name = "Output twin has forward link",
        description = "True if the output twin has at least one forward link of the given link id")
@Slf4j
public class ConditionerOutputTwinHasForwardLink extends Conditioner {

    @FeaturerParam(name = "Link id", description = "Forward link id to check on the output twin", order = 1)
    public static final FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @Lazy
    @Autowired
    private TwinLinkService twinLinkService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        TwinEntity outputTwin = factoryItem.getOutput() != null ? factoryItem.getOutput().getTwinEntity() : null;
        if (outputTwin == null) {
            return false;
        }
        UUID link = linkId.extract(properties);
        if (outputTwin.getTwinLinks() != null) {
            Collection<TwinLinkEntity> links = outputTwin.getTwinLinks().getForwardLinks().getGrouped(link);
            return CollectionUtils.isNotEmpty(links);
        }
        return twinLinkService.hasLink(outputTwin, link);
    }
}
