package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinSearchServiceV2;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2445,
        name = "Twin exists by head of head, link and field equals",
        description = "True if twin exists with same head of head, dst link, numeric field value and optional dst twin assignee.")
@Slf4j
public class ConditionerTwinExistsByHeadOfHeadLinkAndFieldEquals extends ConditionerTwinExistsByTwinLinkAndFieldEqualsBase {

    public ConditionerTwinExistsByHeadOfHeadLinkAndFieldEquals(TwinSearchServiceV2 twinSearchService, TwinClassFieldService twinClassFieldService, TwinLinkService twinLinkService, TwinService twinService) {
        super(twinSearchService, twinClassFieldService, twinLinkService, twinService);
    }

    @Override
    protected UUID resolveHeadTwinId(TwinEntity contextTwin) {
        if (contextTwin.getHeadTwinId() != null) {
            TwinEntity headTwin = contextTwin.getHeadTwin();
            if (headTwin == null) {
                headTwin = twinService.findHeadTwin(contextTwin.getHeadTwinId());
            }
            if (headTwin != null && headTwin.getHeadTwinId() != null) {
                return headTwin.getHeadTwinId();
            }
            return null;
        }
        return null;
    }
}
