package org.twins.core.featurer.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinStatusId;

import java.util.List;
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

    @FeaturerParam(name = "Link Ids", description = "Link ids to check for src twins")
    public static final FeaturerParamUUIDSetTwinsLinkId linkIds = new FeaturerParamUUIDSetTwinsLinkId("linkIds");

    @FeaturerParam(name = "Src Twins Statuses", description = "Src twins statuses to check for remaining twins")
    public static final FeaturerParamUUIDSetTwinsStatusId srcTwinsStatuses = new FeaturerParamUUIDSetTwinsStatusId("srcTwinsStatuses");

    @FeaturerParam(name = "Dst Twin Dst Status Id", description = "Status ID to set for dst twin when no matching src twins remain")
    public static final FeaturerParamUUIDTwinsTwinStatusId dstTwinDstStatusId = new FeaturerParamUUIDTwinsTwinStatusId("dstTwinDstStatusId");

    @Lazy
    final TwinRepository twinRepository;

    @Lazy
    final TwinLinkRepository twinLinkRepository;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus, UUID jobTwinId) throws ServiceException {
        Set<UUID> linkIdsValue = linkIds.extract(properties);
        Set<UUID> srcTwinsStatusesValue = srcTwinsStatuses.extract(properties);
        UUID dstTwinDstStatusIdValue = dstTwinDstStatusId.extract(properties);

        if (linkIdsValue == null || linkIdsValue.isEmpty()) {
            log.warn("ChangeDstTwinStatusByLinkIfNoSrcTwinsInStatuses: skip for {}, linkIds is empty", twinEntity.logNormal());
            return;
        }

        if (srcTwinsStatusesValue == null || srcTwinsStatusesValue.isEmpty()) {
            log.warn("ChangeDstTwinStatusByLinkIfNoSrcTwinsInStatuses: skip for {}, srcTwinsStatuses is empty", twinEntity.logNormal());
            return;
        }

        log.info("ChangeDstTwinStatusByLinkIfNoSrcTwinsInStatuses: executing for {} with params: linkIds={}, srcTwinsStatuses={}, dstTwinDstStatusId={}",
                twinEntity.logNormal(), linkIdsValue, srcTwinsStatusesValue, dstTwinDstStatusIdValue);

        int totalUpdated = 0;
        for (UUID linkId : linkIdsValue) {
            // Find all dst twins where current twin is src
            List<UUID> dstTwinIds = twinLinkRepository.findDstTwinIdsBySrcTwinIdAndLinkId(twinEntity.getId(), linkId);
            for (UUID dstTwinId : dstTwinIds) {
                int updated = twinRepository.updateDstTwinStatusByLinkIfNoSrcTwinsInStatuses(
                        dstTwinId, twinEntity.getId(), linkId, srcTwinsStatusesValue, dstTwinDstStatusIdValue);
                totalUpdated += updated;
                if (updated > 0) {
                    log.info("ChangeDstTwinStatusByLinkIfNoSrcTwinsInStatuses: updated dst twin {} for link {}", dstTwinId, linkId);
                }
            }
        }

        log.info("ChangeDstTwinStatusByLinkIfNoSrcTwinsInStatuses: total updated {} dst twins", totalUpdated);
    }
}
