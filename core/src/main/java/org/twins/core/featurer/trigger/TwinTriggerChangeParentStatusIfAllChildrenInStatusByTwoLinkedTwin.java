package org.twins.core.featurer.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinStatusId;

import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1520,
        name = "ChangeTwinStatusIfAllLinkedTwinsInStatus",
        description = "Change twin status if all linked twin have the target status")
@RequiredArgsConstructor
public class TwinTriggerChangeParentStatusIfAllChildrenInStatusByTwoLinkedTwin extends TwinTrigger {

    @FeaturerParam(name = "First hop link id", description = "Link ID for first hop (t1 -> t2). If omitted, t1.headTwinId = t2.id (head)", optional = true)
    public static final FeaturerParamUUIDTwinsLinkId firstHopLinkId = new FeaturerParamUUIDTwinsLinkId("firstHopLinkId");

    @FeaturerParam(name = "Second hop link id", description = "Link ID for second hop (t2 -> t3). If omitted, t2.headTwinId = t3.id (head)", optional = true)
    public static final FeaturerParamUUIDTwinsLinkId secondHopLinkId = new FeaturerParamUUIDTwinsLinkId("secondHopLinkId");

    @FeaturerParam(name = "Parent dst status id", description = "Status ID to set for parent when condition is met. If omitted, uses destination status from transition")
    public static final FeaturerParamUUIDTwinsTwinStatusId dstStatusId = new FeaturerParamUUIDTwinsTwinStatusId("dstStatusId");

    @FeaturerParam(name = "Children status id", description = "Status ID to check against for children. If omitted, uses destination status from transition (i.e., checks if all children are in the same status as t1 is transitioning to)")
    public static final FeaturerParamUUIDTwinsTwinStatusId linkedTwinStatusId = new FeaturerParamUUIDTwinsTwinStatusId("linkedTwinStatusId");

    @Lazy
    final TwinRepository twinRepository;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus, UUID jobTwinId) throws ServiceException {
        UUID firstHopLinkIdValue = firstHopLinkId.extract(properties);
        UUID secondHopLinkIdValue = secondHopLinkId.extract(properties);
        UUID dstStatusIdValue = dstStatusId.extract(properties);
        UUID linkedTwinStatusIdValue = linkedTwinStatusId.extract(properties);

        boolean firstByHead = firstHopLinkIdValue == null;
        boolean secondByHead = secondHopLinkIdValue == null;

        int updated = updateParentStatus(twinEntity, firstByHead, firstHopLinkIdValue, secondByHead, secondHopLinkIdValue, dstStatusIdValue, linkedTwinStatusIdValue);

        if (updated > 0) {
            log.info("ChangeTwinStatusIfAllLinkedTwinsInStatus: updated {} parent twins", updated);
        }
    }

    private int updateParentStatus(TwinEntity twinEntity, boolean firstByHead, UUID firstHopLinkIdValue,
                                   boolean secondByHead, UUID secondHopLinkIdValue,
                                   UUID parentTargetStatusId, UUID checkChildrenStatusId) {
        // Case 1: head-head
        // t1 -> t2 by head (t1.headTwinId = t2.id)
        // t2 -> t3 by head (t2.headTwinId = t3.id)
        // Check all children of t3 by head
        if (firstByHead && secondByHead) {
            return twinRepository.updateGrandparentStatusIfAllGrandchildrenInStatusHeadHead(
                    twinEntity.getId(), parentTargetStatusId, checkChildrenStatusId);
        }

        // Case 2: head-link
        // t1 -> t2 by head (t1.headTwinId = t2.id)
        // t2 -> t3 by link (t2 link -> t3)
        // Check all children of t3 by link
        if (firstByHead) {
            return twinRepository.updateGrandparentStatusIfAllGrandchildrenInStatusHeadLink(
                    twinEntity.getId(), secondHopLinkIdValue, parentTargetStatusId, checkChildrenStatusId);
        }

        // Case 3: link-link
        // t1 -> t2 by link (t1 link -> t2)
        // t2 -> t3 by link (t2 link -> t3)
        // Check all children of t3 by link
        if (!secondByHead) {
            return twinRepository.updateGrandparentStatusIfAllGrandchildrenInStatusLinkLink(
                    twinEntity.getId(), firstHopLinkIdValue, secondHopLinkIdValue, parentTargetStatusId, checkChildrenStatusId);
        }

        // Case 4: link-head
        // t1 -> t2 by link (t1 link -> t2)
        // t2 -> t3 by head (t2.headTwinId = t3.id)
        // Check all children of t3 by head
        return twinRepository.updateGrandparentStatusIfAllGrandchildrenInStatusLinkHead(
                twinEntity.getId(), firstHopLinkIdValue, parentTargetStatusId, checkChildrenStatusId);

    }
}
