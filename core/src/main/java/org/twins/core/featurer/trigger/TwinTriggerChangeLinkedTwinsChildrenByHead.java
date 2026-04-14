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
@Featurer(id = FeaturerTwins.ID_1512,
        name = "ChangeStatusForLinkedTwinsChildrenByHead",
        description = "Change status for children of linked twins (via backward link then head_twin_id)")
@RequiredArgsConstructor
public class TwinTriggerChangeLinkedTwinsChildrenByHead extends TwinTrigger {

    @FeaturerParam(name = "LinkId", description = "Link ID to find linked twins")
    public static final FeaturerParamUUID linkId = new FeaturerParamUUID("linkId");

    @FeaturerParam(name = "ClassId", description = "Class ID of children to update")
    public static final FeaturerParamUUID classId = new FeaturerParamUUID("classId");

    @FeaturerParam(name = "dstStatusId", description = "Status ID to set")
    public static final FeaturerParamUUID dstStatusId = new FeaturerParamUUID("dstStatusId");

    @Lazy
    final TwinRepository twinRepository;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        log.warn("ChangeLinkedTwinsChildrenByHead: START - twinId={}, class={}",
            twinEntity.getId(), twinEntity.getTwinClassId());

        UUID linkIdValue = linkId.extract(properties);
        UUID classIdValue = classId.extract(properties);
        UUID dstStatusIdValue = dstStatusId.extract(properties);

        if (linkIdValue == null || dstStatusIdValue == null) {
            log.warn("ChangeLinkedTwinsChildrenByHead: missing parameters");
            return;
        }

        log.info("ChangeLinkedTwinsChildrenByHead: executing update - twinId={}, linkId={}, classId={}, statusId={}",
            twinEntity.getId(), linkIdValue, classIdValue, dstStatusIdValue);

        int updated = twinRepository.updateTwinStatusByLinkAndHeadTwinChildren(twinEntity.getId(), linkIdValue, classIdValue, dstStatusIdValue);
        log.warn("ChangeLinkedTwinsChildrenByHead: updated {} children twins", updated);
    }
}
