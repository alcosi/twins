package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.link.TwinLinkService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2446,
        name = "Twin exists by head and context link dst",
        description = "True if twin exists with same head twin and link dst twin resolved from context twin forward link.")
@Slf4j
public class ConditionerTwinExistsByHeadAndContextLinkDst extends ConditionerTwinExistsByHeadAndLinkDstBase {

    @Lazy
    @Autowired
    private TwinLinkService twinLinkService;

    @Override
    protected UUID resolveHeadTwinId(TwinEntity contextTwin) throws ServiceException {
        return twinHeadService.resolveHeadTwinId(contextTwin, null);
    }

    @Override
    protected UUID resolveDstTwinId(Properties properties, FactoryItem factoryItem, TwinEntity contextTwin) throws ServiceException {
        UUID linkId = dstLinkId.extract(properties);
        twinLinkService.loadTwinLinks(contextTwin);
        try {
            var linkEntity = contextTwin.getTwinLinks().getForwardLinks().getGrouped(linkId).getFirst();
            return linkEntity.getDstTwinId();
        } catch (Exception e) {
            log.debug("Link dst twin not found by link [{}] on context twin [{}]", linkId, contextTwin.logShort());
            return null;
        }
    }
}
