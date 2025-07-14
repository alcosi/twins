package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;

import java.util.Properties;

public interface FieldTyperCountChildrenByTwinClass {

    @FeaturerParam(name = "Of twin class id", description = "")
    FeaturerParamUUID ofTwinClassId = new FeaturerParamUUIDTwinsTwinClassId("ofTwinClassId");

    default Long getCountResult(Properties properties, TwinEntity twinEntity, TwinFieldSimpleRepository twinFieldSimpleRepository) {
        return twinFieldSimpleRepository.countChildrenTwinsByTwinClassId(twinEntity.getId(), ofTwinClassId.extract(properties));
    }
}
