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
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinStatusId;

import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1515,
        name = "ChangeStatusByLinkThenHead",
        description = "Change status for ancestor found via link then head_twin_id")
@RequiredArgsConstructor
public class TwinTriggerChangeStatusByLinkThenHead extends TwinTrigger {

    @FeaturerParam(name = "linkId", description = "Link ID to find intermediate twin")
    public static final FeaturerParamUUIDTwinsLinkId linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @FeaturerParam(name = "Class Id", description = "Class ID of ancestor to update")
    public static final FeaturerParamUUIDTwinsTwinClassId classId = new FeaturerParamUUIDTwinsTwinClassId("classId");

    @FeaturerParam(name = "Dst Status Id", description = "Status ID to set")
    public static final FeaturerParamUUIDTwinsTwinStatusId dstStatusId = new FeaturerParamUUIDTwinsTwinStatusId("dstStatusId");

    @Lazy
    final TwinRepository twinRepository;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus, UUID jobTwinId) throws ServiceException {
        UUID linkIdValue = linkId.extract(properties);
        UUID classIdValue = classId.extract(properties);
        UUID dstStatusIdValue = dstStatusId.extract(properties);

        log.info("ChangeStatusByLinkThenHead: executing for {} with params: linkId={}, classId={}, statusId={}",
            twinEntity.logNormal(), linkIdValue, classIdValue, dstStatusIdValue);

        int updated = twinRepository.updateTwinStatusByLinkAndHead(twinEntity.getId(), linkIdValue, classIdValue, dstStatusIdValue);
        log.info("ChangeStatusByLinkThenHead: updated {} ancestors", updated);
    }
}
