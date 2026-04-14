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
@Featurer(id = FeaturerTwins.ID_1517,
        name = "ChangeStatusByHeadThenLink",
        description = "Change status for twin found via head_twin_id then backward link")
@RequiredArgsConstructor
public class TwinTriggerChangeStatusByHeadThenLink extends TwinTrigger {

    @FeaturerParam(name = "linkId", description = "Link ID from head twin to target")
    public static final FeaturerParamUUID linkId = new FeaturerParamUUID("linkId");

    @FeaturerParam(name = "classId", description = "Class ID of target twin to update")
    public static final FeaturerParamUUID classId = new FeaturerParamUUID("classId");

    @FeaturerParam(name = "dstStatusId", description = "Status ID to set")
    public static final FeaturerParamUUID dstStatusId = new FeaturerParamUUID("dstStatusId");

    @Lazy
    final TwinRepository twinRepository;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        UUID linkIdValue = linkId.extract(properties);
        UUID classIdValue = classId.extract(properties);
        UUID dstStatusIdValue = dstStatusId.extract(properties);

        if (linkIdValue == null || classIdValue == null || dstStatusIdValue == null) {
            log.warn("ChangeStatusByHeadThenLink: missing parameters");
            return;
        }

        log.info("ChangeStatusByHeadThenLink: executing update - twinId={}, linkId={}, classId={}, statusId={}",
            twinEntity.getId(), linkIdValue, classIdValue, dstStatusIdValue);

        int updated = twinRepository.updateTwinStatusByHeadThenLinkId(twinEntity.getId(), linkIdValue, classIdValue, dstStatusIdValue);
        log.info("ChangeStatusByHeadThenLink: updated {} targets", updated);
    }
}
