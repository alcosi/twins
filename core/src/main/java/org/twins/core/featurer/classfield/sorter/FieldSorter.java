package org.twins.core.featurer.classfield.sorter;

import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.search.SearchSorter;

@FeaturerType(id = FeaturerTwins.TYPE_40,
        name = "FieldSorter",
        description = "Order class fields search")
public abstract class FieldSorter extends SearchSorter<TwinClassFieldEntity> {
}
