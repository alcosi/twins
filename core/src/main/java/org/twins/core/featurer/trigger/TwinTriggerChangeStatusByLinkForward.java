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
@Featurer(id = FeaturerTwins.ID_1514,
        name = "ChangeStatusByLinkForward",
        description = "Change status for destination twin connected via forward link")
@RequiredArgsConstructor
public class TwinTriggerChangeStatusByLinkForward extends TwinTrigger {

    @FeaturerParam(name = "linkId", description = "Link ID (same direction as forward link)")
    public static final FeaturerParamUUIDTwinsLinkId linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @FeaturerParam(name = "Dst Status Id", description = "Status ID to set for destination twin")
    public static final FeaturerParamUUIDTwinsTwinStatusId dstStatusId = new FeaturerParamUUIDTwinsTwinStatusId("dstStatusId");

    @Lazy
    final TwinRepository twinRepository;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus, UUID jobTwinId) throws ServiceException {
        UUID linkIdValue = linkId.extract(properties);
        UUID dstStatusIdValue = dstStatusId.extract(properties);

        log.info("ChangeStatusByLinkForward: executing for {} with params: linkId={}, statusId={}",
            twinEntity.logNormal(), linkIdValue, dstStatusIdValue);

        int updated = twinRepository.updateTwinStatusBySrcTwinIdAndLinkId(twinEntity.getId(), linkIdValue, null, dstStatusIdValue);
        log.info("ChangeStatusByLinkForward: updated {} twins", updated);
    }
}
