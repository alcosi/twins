package org.twins.core.featurer.datalist.sorter;

import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.search.SearchSorter;

@FeaturerType(id = FeaturerTwins.TYPE_52,
        name = "DataListOptionSorter",
        description = "Order data list option search")
public abstract class DataListOptionSorter extends SearchSorter<DataListOptionEntity> {
}
