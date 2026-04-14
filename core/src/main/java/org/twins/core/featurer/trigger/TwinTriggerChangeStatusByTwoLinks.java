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

import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1513,
        name = "ChangeStatusByTwoLinks",
        description = "Change status for twins connected via two links (twin -> link1 -> intermediate -> link2 -> target)")
@RequiredArgsConstructor
public class TwinTriggerChangeStatusByTwoLinks extends TwinTrigger {

    @FeaturerParam(name = "FirstLinkId", description = "First link ID (from intermediate to current twin)")
    public static final FeaturerParamUUID firstLinkId = new FeaturerParamUUID("firstLinkId");

    @FeaturerParam(name = "SecondLinkId", description = "Second link ID (from target to intermediate)")
    public static final FeaturerParamUUID secondLinkId = new FeaturerParamUUID("secondLinkId");

    @FeaturerParam(name = "ClassId", description = "Class ID of target twins to update")
    public static final FeaturerParamUUID classId = new FeaturerParamUUID("classId");

    @FeaturerParam(name = "dstStatusId", description = "Status ID to set")
    public static final FeaturerParamUUID dstStatusId = new FeaturerParamUUID("dstStatusId");

    @Lazy
    final TwinRepository twinRepository;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        log.warn("ChangeStatusByTwoLinks: START - twinId={}, class={}",
            twinEntity.getId(), twinEntity.getTwinClassId());

        UUID firstLinkIdValue = firstLinkId.extract(properties);
        UUID secondLinkIdValue = secondLinkId.extract(properties);
        UUID classIdValue = classId.extract(properties);
        UUID dstStatusIdValue = dstStatusId.extract(properties);

        log.warn("ChangeStatusByTwoLinks: params - firstLinkId={}, secondLinkId={}, classId={}, dstStatusId={}",
            firstLinkIdValue, secondLinkIdValue, classIdValue, dstStatusIdValue);

        if (firstLinkIdValue == null || secondLinkIdValue == null || dstStatusIdValue == null) {
            log.warn("ChangeStatusByTwoLinks: missing required parameters");
            return;
        }

        int updated = twinRepository.updateTwinStatusByTwoLinks(twinEntity.getId(), firstLinkIdValue, secondLinkIdValue, classIdValue, dstStatusIdValue);
        log.warn("ChangeStatusByTwoLinks: updated {} target twins", updated);
    }
}
