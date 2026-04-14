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
@Featurer(id = FeaturerTwins.ID_1510,
        name = "CascadeToAncestorsByHead",
        description = "Cascade status change up using head_twin_id to specific class at specific depth")
@RequiredArgsConstructor
public class TwinTriggerCascadeToAncestorsByHead extends TwinTrigger {

    @FeaturerParam(name = "Depth", description = "Hierarchy depth to search up (1 = direct parent only, -1 = unlimited)")
    public static final FeaturerParamInt depth = new FeaturerParamInt("depth");

    @FeaturerParam(name = "Class Id", description = "Twin class ID to update")
    public static final FeaturerParamUUIDTwinsTwinClassId ancestorClassId = new FeaturerParamUUIDTwinsTwinClassId("classId");

    @FeaturerParam(name = "New Status Id", description = "Status ID to set")
    public static final FeaturerParamUUIDTwinsTwinStatusId dstStatusId = new FeaturerParamUUIDTwinsTwinStatusId("dstStatusId");

    @Lazy
    final TwinRepository twinRepository;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        Integer depthValue = depth.extract(properties);
        UUID classIdValue = ancestorClassId.extract(properties);
        UUID dstStatusIdValue = dstStatusId.extract(properties);

        if (depthValue == null || depthValue < 0) {
            depthValue = -1;
        }

        log.info("CascadeToAncestorsByHead: executing for {} with params: depth={}, ancestorClassId={}, statusId={}",
            twinEntity.logNormal(), depthValue, classIdValue, dstStatusIdValue);

        int updated = twinRepository.updateTwinStatusByHeadAncestors(twinEntity.getId(), twinEntity.getHierarchyTree(), depthValue, classIdValue, dstStatusIdValue);
        log.info("CascadeToAncestorsByHead: updated {} ancestors", updated);
    }
}
