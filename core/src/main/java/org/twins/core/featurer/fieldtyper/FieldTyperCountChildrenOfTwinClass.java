package org.twins.core.featurer.fieldtyper;

import org.cambium.common.util.LTreeUtils;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsClassId;

import java.util.Collections;
import java.util.Properties;

public interface FieldTyperCountChildrenOfTwinClass {

    @FeaturerParam(name = "Twin class ids", description = "", order = 1)
    FeaturerParamUUIDSet twinClassIds = new FeaturerParamUUIDSetTwinsClassId("twinClassIds");

    @FeaturerParam(name = "Use extends hierarchy", description = "If true, counts twins from classes that extend the specified twin classes. If false, counts only direct twin class matches.", order = 2, optional = true, defaultValue = "false")
    FeaturerParamBoolean useExtendsHierarchy = new FeaturerParamBoolean("useExtendsHierarchy");

    default Long getCountResult(Properties properties, TwinEntity twinEntity, TwinFieldSimpleRepository twinFieldSimpleRepository) {
        var classIds = twinClassIds.extract(properties);
        boolean useHierarchy = useExtendsHierarchy.extract(properties);

        if (useHierarchy) {
            String lquery = LTreeUtils.buildLQueryFromUuids(classIds);
            var result = twinFieldSimpleRepository.countChildrenTwinsByExtendsHierarchy(
                    Collections.singleton(twinEntity.getId()), lquery);
            return result.isEmpty() ? 0L : result.getFirst().calc().longValue();
        } else {
            var result = twinFieldSimpleRepository.countChildrenTwinsOfTwinClassIdIn(
                    Collections.singleton(twinEntity.getId()), classIds);
            return result.isEmpty() ? 0L : result.getFirst().calc().longValue();
        }
    }
}
