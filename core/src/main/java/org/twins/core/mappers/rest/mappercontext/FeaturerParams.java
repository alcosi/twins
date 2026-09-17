package org.twins.core.mappers.rest.mappercontext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

/**
 * A featurer id paired with its configured params (hstore) from the source entity.
 * Acts as a src object for FeaturerParametrizedRestDTOMapper: produces a FeaturerDTO via
 * FeaturerRestDTOMapper and additionally postpones EntityRef objects for entity-referencing params.
 * Cache key includes the params content: the same featurer can be used with different params
 * in different roles (e.g. fieldTyper and twinSorter) and both walks must happen.
 */
@Getter
@RequiredArgsConstructor
public class FeaturerParams {
    final Integer featurerId;
    final HashMap<String, String> params;

    public String cacheKey() {
        return featurerId + "|" + (params == null ? 0 : params.hashCode());
    }

    @Override
    public String toString() {
        return "featurerId[" + featurerId + "] params[" + params + "]";
    }
}
