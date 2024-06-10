package org.twins.core.featurer.search.criteriabuilder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Lazy
@Component
@Featurer(id = 2701,
        name = "SearchCriteriaBuilderConfiguredId",
        description = "")
public class SearchCriteriaBuilderConfiguredId extends SearchCriteriaBuilderSingleUUID {
    @FeaturerParam(name = "entityId", description = "")
    public static final FeaturerParamUUID entityId = new FeaturerParamUUID("entityId"); //todo UI problems (TWINS-113)

    @Override
    protected UUID getId(Properties properties, Map<String, String> namedParamsMap) {
        return entityId.extract(properties);
    }
}
