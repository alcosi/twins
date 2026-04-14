package org.twins.core.featurer.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinStatusId;

import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1508,
        name = "CascadeToDescendants",
        description = "Cascade status change down using head_twin_id to specific class at specific depth")
@RequiredArgsConstructor
public class TwinTriggerCascadeToDescendants extends TwinTrigger {

    @FeaturerParam(name = "Depth", description = "Hierarchy depth to search (1 = direct children, 2 = grandchildren, etc.)")
    public static final FeaturerParamInt depth = new FeaturerParamInt("depth");

    @FeaturerParam(name = "Class Id", description = "Twin class ID to update")
    public static final FeaturerParamUUIDTwinsTwinClassId classId = new FeaturerParamUUIDTwinsTwinClassId("classId");

    @FeaturerParam(name = "Dst Status Id", description = "Status ID to set")
    public static final FeaturerParamUUIDTwinsTwinStatusId dstStatusId = new FeaturerParamUUIDTwinsTwinStatusId("dstStatusId");

    @Lazy
    final TwinRepository twinRepository;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        Integer depthValue = depth.extract(properties);
        UUID classIdValue = classId.extract(properties);
        UUID dstStatusIdValue = dstStatusId.extract(properties);

        log.info("CascadeToDescendants: executing for {} with params: depth={}, classId={}, statusId={}",
            twinEntity.logNormal(), depthValue, classIdValue, dstStatusIdValue);

        int updated = twinRepository.updateTwinStatusByHeadDescendants(
            twinEntity.getId(), depthValue, classIdValue, dstStatusIdValue);
        log.info("CascadeToDescendants: updated {} descendants", updated);
    }
}
