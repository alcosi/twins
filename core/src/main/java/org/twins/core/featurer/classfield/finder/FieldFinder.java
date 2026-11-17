package org.twins.core.featurer.classfield.finder;

import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.search.SearchFinder;

@FeaturerType(id = FeaturerTwins.TYPE_32,
        name = "FieldFinder",
        description = "Find class fields")
public abstract class FieldFinder extends SearchFinder<TwinClassFieldSearch> {
    public static final String PARAM_CURRENT_TWIN_CLASS_ID = "currentTwinClassId";
    public static final String PARAM_CURRENT_TWIN_ID = "currentTwinId";
}
