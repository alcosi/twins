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
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinStatusId;

import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1509,
        name = "TwinTriggerChangeStatusByLink",
        description = "")
@RequiredArgsConstructor
public class TwinTriggerChangeStatusByLink extends TwinTrigger {

    @FeaturerParam(name = "LinkId", description = "Link ID")
    public static final FeaturerParamUUIDTwinsLinkId linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @FeaturerParam(name = "dstStatusId", description = "Status ID")
    public static final FeaturerParamUUIDTwinsTwinStatusId dstStatusId = new FeaturerParamUUIDTwinsTwinStatusId("dstStatusId");

    @FeaturerParam(name = "classId", description = "Class ID")
    public static final FeaturerParamUUIDTwinsTwinClassId twinClassId = new FeaturerParamUUIDTwinsTwinClassId("classId");

    @Lazy
    final TwinRepository twinRepository;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        UUID linkIdValue = linkId.extract(properties);
        UUID dstStatusIdValue = dstStatusId.extract(properties);
        UUID classIdValue = twinClassId.extract(properties);

        log.info("TwinTriggerChangeStatusByLink: executing update - twinId={}, linkId={}, classId={}, statusId={}",
            twinEntity.logShort(), linkIdValue, classIdValue, dstStatusIdValue);

        int updated = twinRepository.updateTwinStatusByTwinClassIdAndLinkId(twinEntity.getId(), linkIdValue, classIdValue, dstStatusIdValue);
        log.info("TwinTriggerChangeStatusByLink: updated {} twins", updated);
    }
}
