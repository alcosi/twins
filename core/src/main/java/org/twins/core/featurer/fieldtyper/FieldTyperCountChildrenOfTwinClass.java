package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsClassId;

import java.util.Properties;

public interface FieldTyperCountChildrenOfTwinClass {

    @FeaturerParam(name = "Twin class ids", description = "", order = 1)
    FeaturerParamUUIDSet twinClassIds = new FeaturerParamUUIDSetTwinsClassId("twinClassIds");

    default Long getCountResult(Properties properties, TwinEntity twinEntity, TwinFieldSimpleRepository twinFieldSimpleRepository) {
        return twinFieldSimpleRepository.countChildrenTwinsOfTwinClassIdIn(twinEntity.getId(), twinClassIds.extract(properties));
    }
}
