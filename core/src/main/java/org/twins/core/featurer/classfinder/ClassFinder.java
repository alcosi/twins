package org.twins.core.featurer.classfinder;

import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.search.SearchFinder;

@FeaturerType(id = FeaturerTwins.TYPE_39,
        name = "ClassFinder",
        description = "Find classes")
public abstract class ClassFinder extends SearchFinder<TwinClassSearch> {
}
