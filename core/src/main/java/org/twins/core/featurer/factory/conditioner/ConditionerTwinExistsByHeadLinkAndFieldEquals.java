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
@Featurer(id = FeaturerTwins.ID_2443,
        name = "Twin exists by head, link and field equals",
        description = "True if twin exists with same head, dst link, numeric field value and optional dst twin assignee.")
@Slf4j
public class ConditionerTwinExistsByHeadLinkAndFieldEquals extends ConditionerTwinExistsByTwinLinkAndFieldEqualsBase {

    public ConditionerTwinExistsByHeadLinkAndFieldEquals(TwinSearchServiceV2 twinSearchService, TwinClassFieldService twinClassFieldService, TwinLinkService twinLinkService, TwinService twinService) {
        super(twinSearchService, twinClassFieldService, twinLinkService, twinService);
    }

    @Override
    protected UUID resolveHeadTwinId(TwinEntity contextTwin) {
        return contextTwin.getHeadTwinId() != null ? contextTwin.getHeadTwinId() : contextTwin.getId();
    }
}
