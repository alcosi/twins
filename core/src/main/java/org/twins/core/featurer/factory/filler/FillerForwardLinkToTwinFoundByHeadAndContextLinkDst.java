package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(
        id = FeaturerTwins.ID_2358,
        name = "Forward link to twin found by head and context link dst",
        description = "Finds twin by head and link dst resolved from context twin forward link; creates forward link from output twin."
)
@Slf4j
public class FillerForwardLinkToTwinFoundByHeadAndContextLinkDst extends FillerForwardLinkToTwinFoundByHeadAndLinkDstBase {

    @FeaturerParam(name = "Dst link id", description = "Link id for search by link dst twin", order = 3)
    public static final FeaturerParamUUID dstLinkId = new FeaturerParamUUIDTwinsLinkId("dstLinkId");

    @Override
    protected UUID getLinkId(Properties properties) throws ServiceException {
        return dstLinkId.extract(properties);
    }

    @Override
    protected UUID resolveDstTwinId(Properties properties, FactoryItem factoryItem, TwinEntity contextTwin) throws ServiceException {
        UUID linkId = dstLinkId.extract(properties);
        twinLinkService.loadTwinLinks(contextTwin);

        try {
            List<TwinLinkEntity> forwardLinks = contextTwin.getTwinLinks().getForwardLinks().getGrouped(linkId);
            if (forwardLinks == null || forwardLinks.isEmpty()) {
                log.debug("Link dst twin not found by link [{}] on context twin [{}]", linkId, contextTwin.logShort());
                return null;
            }
            var linkEntity = forwardLinks.getFirst();
            if (linkEntity.getDstTwin() != null) {
                return linkEntity.getDstTwin().getId();
            }
            return linkEntity.getDstTwinId();
        } catch (Exception e) {
            log.debug("Link dst twin resolve failed by link [{}] on context twin [{}]: {}", linkId, contextTwin.logShort(), e.getMessage());
            return null;
        }
    }
}

