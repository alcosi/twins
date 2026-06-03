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
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinStatusId;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1519,
        name = "ChangeDstTwinStatusByLinkIfNoSrcTwinsInStatuses",
        description = "Change dst twin status by link if there are no src twins in monitored statuses")
@RequiredArgsConstructor
public class TwinTriggerChangeDstTwinStatusByLinkIfNoSrcTwinsInStatuses extends TwinTrigger {

    @FeaturerParam(name = "Link Id", description = "Link id to check for linked twins")
    public static final FeaturerParamUUIDTwinsLinkId linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @FeaturerParam(name = "Linked Twins Statuses", description = "Linked twins statuses to check for remaining twins")
    public static final FeaturerParamUUIDSetTwinsStatusId linkedTwinInStatuses = new FeaturerParamUUIDSetTwinsStatusId("linkedTwinInStatuses");

    @FeaturerParam(name = "Dst Twin Status Id", description = "Status ID to set for dst twin when no matching src twins remain")
    public static final FeaturerParamUUIDTwinsTwinStatusId dstTwinDstStatusId = new FeaturerParamUUIDTwinsTwinStatusId("dstTwinDstStatusId");

    @Lazy
    final TwinRepository twinRepository;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus, UUID jobTwinId) throws ServiceException {
        UUID linkIdExtracted = linkId.extract(properties);
        Set<UUID> srcTwinsStatusesValue = linkedTwinInStatuses.extract(properties);
        UUID dstTwinDstStatusIdExtracted = dstTwinDstStatusId.extract(properties);

        if (linkIdExtracted == null) {
            log.warn("ChangeDstTwinStatusByLinkIfNoSrcTwinsInStatuses: skip for {}, linkIds is empty", twinEntity.logNormal());
            return;
        }

        if (srcTwinsStatusesValue == null || srcTwinsStatusesValue.isEmpty()) {
            log.warn("ChangeDstTwinStatusByLinkIfNoSrcTwinsInStatuses: skip for {}, srcTwinsStatuses is empty", twinEntity.logNormal());
            return;
        }

        log.info("ChangeDstTwinStatusByLinkIfNoSrcTwinsInStatuses: executing for {} with params: linkId={}, srcTwinsStatuses={}, dstTwinDstStatusId={}",
                twinEntity.logNormal(), linkIdExtracted, srcTwinsStatusesValue, dstTwinDstStatusIdExtracted);

        int updated = twinRepository.updateDstTwinStatusByLinkIfNoLinkedTwinsInStatuses(
                twinEntity.getId(), linkIdExtracted, srcTwinsStatusesValue, dstTwinDstStatusIdExtracted);

        log.info("ChangeDstTwinStatusByLinkIfNoSrcTwinsInStatuses: updated {} dst twins", updated);
    }
}
