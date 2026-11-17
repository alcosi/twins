package org.twins.core.featurer.classfinder.sorter;

import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.search.SearchSorter;

@FeaturerType(id = FeaturerTwins.TYPE_58,
        name = "ClassSorter",
        description = "Order classes search")
public abstract class ClassSorter extends SearchSorter<TwinClassEntity> {
}
