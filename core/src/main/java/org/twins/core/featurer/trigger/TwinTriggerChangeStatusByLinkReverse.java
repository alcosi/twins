package org.twins.core.featurer.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
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
@Featurer(id = FeaturerTwins.ID_1511,
        name = "ChangeStatusByLinkReverse",
        description = "Change status for source twin connected via backward link")
@RequiredArgsConstructor
public class TwinTriggerChangeStatusByLinkReverse extends TwinTrigger {

    @FeaturerParam(name = "linkId", description = "Link ID (find backward link)")
    public static final FeaturerParamUUIDTwinsLinkId linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @FeaturerParam(name = "dstStatusId", description = "Status ID to set for source twin")
    public static final FeaturerParamUUIDTwinsTwinStatusId dstStatusId = new FeaturerParamUUIDTwinsTwinStatusId("dstStatusId");

    @Lazy
    final TwinRepository twinRepository;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        UUID linkIdValue = linkId.extract(properties);
        UUID dstStatusIdValue = dstStatusId.extract(properties);

        log.info("ChangeStatusByLinkReverse: executing update - twinId={}, linkId={}, statusId={}",
            twinEntity.logShort(), linkIdValue, dstStatusIdValue);

        int updated = twinRepository.updateTwinStatusByDstTwinIdAndLinkId(twinEntity.getId(), linkIdValue, dstStatusIdValue);
        log.info("ChangeStatusByLinkReverse: updated {} source twins", updated);
    }
}
