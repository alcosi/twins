package org.twins.core.featurer.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1511,
        name = "ChangeStatusByLinkReverse",
        description = "Change status for source twin connected via backward link")
@RequiredArgsConstructor
public class TwinTriggerChangeStatusByLinkReverse extends TwinTrigger {

    @FeaturerParam(name = "dstStatusId", description = "Status ID to set for source twin")
    public static final FeaturerParamUUID dstStatusId = new FeaturerParamUUID("dstStatusId");

    @FeaturerParam(name = "LinkId", description = "Link ID (same direction as forward link)")
    public static final FeaturerParamUUID linkId = new FeaturerParamUUID("linkId");

    @Lazy
    final TwinService twinService;
    @Lazy
    final TwinLinkService twinLinkService;
    @Lazy
    final LinkService linkService;
    @Lazy
    final TwinStatusService twinStatusService;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        log.warn("ChangeStatusByLinkReverse: START - twinId={}, class={}",
            twinEntity.getId(), twinEntity.getTwinClassId());

        UUID dstStatusIdValue = dstStatusId.extract(properties);
        UUID linkIdValue = linkId.extract(properties);

        log.warn("ChangeStatusByLinkReverse: params - linkId={}, dstStatusId={}",
            linkIdValue, dstStatusIdValue);

        if (dstStatusIdValue == null || linkIdValue == null) {
            log.warn("ChangeStatusByLinkReverse: missing parameters");
            return;
        }

        LinkEntity link = linkService.findEntitySafe(linkIdValue);
        var twinLinks = twinLinkService.findTwinLinks(link, twinEntity, LinkService.LinkDirection.backward);

        log.warn("ChangeStatusByLinkReverse: found {} twin links", twinLinks.size());

        if (!twinLinks.isEmpty()) {
            TwinStatusEntity status = twinStatusService.findEntitySafe(dstStatusIdValue);

            List<TwinEntity> sourceTwins = twinLinks.stream()
                .map(TwinLinkEntity::getSrcTwin)
                .filter(Objects::nonNull)
                .toList();

            if (!sourceTwins.isEmpty()) {
                twinService.changeStatus(sourceTwins, status);
                log.warn("ChangeStatusByLinkReverse: changed status for {} source twins for twin {}", sourceTwins.size(), twinEntity.logShort());
            }
        }
    }
}
