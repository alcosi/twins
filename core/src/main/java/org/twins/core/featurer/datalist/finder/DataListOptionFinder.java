package org.twins.core.featurer.datalist.finder;

import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.search.SearchFinder;

@FeaturerType(id = FeaturerTwins.TYPE_51,
        name = "DataListOptionFinder",
        description = "Find data list options")
public abstract class DataListOptionFinder extends SearchFinder<DataListOptionSearch> {
    public static final String PARAM_CURRENT_HEAD_TWIN_ID = "headTwinId";
    public static final String PARAM_CURRENT_TWIN_ID = "twinId";
    public static final String PARAM_CURRENT_TWIN_CLASS_FIELD_ID = "twinClassFieldId";
}
