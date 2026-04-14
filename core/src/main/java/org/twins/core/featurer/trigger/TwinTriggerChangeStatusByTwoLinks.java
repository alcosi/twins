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
@Featurer(id = FeaturerTwins.ID_1513,
        name = "ChangeStatusByTwoLinks",
        description = "Change status for twins connected via two links (twin -> link1 -> intermediate -> link2 -> target)")
@RequiredArgsConstructor
public class TwinTriggerChangeStatusByTwoLinks extends TwinTrigger {

    @FeaturerParam(name = "First Link Id", description = "First link ID (from intermediate to current twin)")
    public static final FeaturerParamUUIDTwinsLinkId firstLinkId = new FeaturerParamUUIDTwinsLinkId("firstLinkId");

    @FeaturerParam(name = "Second Link Id", description = "Second link ID (from target to intermediate)")
    public static final FeaturerParamUUIDTwinsLinkId secondLinkId = new FeaturerParamUUIDTwinsLinkId("secondLinkId");

    @FeaturerParam(name = "Class Id", description = "Class ID of target twins to update")
    public static final FeaturerParamUUIDTwinsTwinClassId classId = new FeaturerParamUUIDTwinsTwinClassId("classId");

    @FeaturerParam(name = "Dst Status Id", description = "Status ID to set")
    public static final FeaturerParamUUIDTwinsTwinStatusId dstStatusId = new FeaturerParamUUIDTwinsTwinStatusId("dstStatusId");

    @Lazy
    final TwinRepository twinRepository;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        UUID firstLinkIdValue = firstLinkId.extract(properties);
        UUID secondLinkIdValue = secondLinkId.extract(properties);
        UUID classIdValue = classId.extract(properties);
        UUID dstStatusIdValue = dstStatusId.extract(properties);

        log.info("ChangeStatusByTwoLinks: executing for {} with params:  firstLinkId={}, secondLinkId={}, classId={}, dstStatusId={}",
            twinEntity.logNormal(), firstLinkIdValue, secondLinkIdValue, classIdValue, dstStatusIdValue);

        int updated = twinRepository.updateTwinStatusByTwoLinks(twinEntity.getId(), firstLinkIdValue, secondLinkIdValue, classIdValue, dstStatusIdValue);
        log.info("ChangeStatusByTwoLinks: updated {} target twins", updated);
    }
}
