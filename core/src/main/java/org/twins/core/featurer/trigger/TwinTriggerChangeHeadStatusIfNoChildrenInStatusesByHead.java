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
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinStatusId;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1518,
        name = "ChangeHeadStatusIfNoChildrenInStatusesByHead",
        description = "Change direct head status if there are no children in monitored statuses")
@RequiredArgsConstructor
public class TwinTriggerChangeHeadStatusIfNoChildrenInStatusesByHead extends TwinTrigger {

    @FeaturerParam(name = "Children Statuses", description = "Children statuses to check for remaining twins")
    public static final FeaturerParamUUIDSetTwinsStatusId childrenStatuses = new FeaturerParamUUIDSetTwinsStatusId("childrenStatuses");

    @FeaturerParam(name = "Head Class Id", description = "Optional head class ID filter")
    public static final FeaturerParamUUIDTwinsTwinClassId headClassId = new FeaturerParamUUIDTwinsTwinClassId("headClassId");

    @FeaturerParam(name = "Head Dst Status Id", description = "Status ID to set for head twin when no matching children remain")
    public static final FeaturerParamUUIDTwinsTwinStatusId headDstStatusId = new FeaturerParamUUIDTwinsTwinStatusId("headDstStatusId");

    @Lazy
    final TwinRepository twinRepository;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus, UUID jobTwinId) throws ServiceException {
        UUID headTwinId = twinEntity.getHeadTwinId();
        if (headTwinId == null) {
            log.info("ChangeHeadStatusIfNoChildrenInStatusesByHead: skip for {}, no head twin", twinEntity.logNormal());
            return;
        }

        Set<UUID> childrenStatusesValue = childrenStatuses.extract(properties);
        UUID headClassIdValue = headClassId.extract(properties);
        UUID headDstStatusIdValue = headDstStatusId.extract(properties);
        if (childrenStatusesValue == null || childrenStatusesValue.isEmpty()) {
            log.warn("ChangeHeadStatusIfNoChildrenInStatusesByHead: skip for {}, childrenStatuses is empty", twinEntity.logNormal());
            return;
        }

        log.info("ChangeHeadStatusIfNoChildrenInStatusesByHead: executing for {} with params: headTwinId={}, childrenStatuses={}, headClassId={}, headDstStatusId={}",
                twinEntity.logNormal(), headTwinId, childrenStatusesValue, headClassIdValue, headDstStatusIdValue);

        int updated = twinRepository.updateHeadStatusIfNoChildrenInStatuses(headTwinId, twinEntity.getId(), childrenStatusesValue, headClassIdValue, headDstStatusIdValue);
        log.info("ChangeHeadStatusIfNoChildrenInStatusesByHead: updated {} head twins", updated);
    }
}
