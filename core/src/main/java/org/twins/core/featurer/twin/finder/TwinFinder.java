package org.twins.core.featurer.twin.finder;

import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.search.SearchFinder;


@FeaturerType(id = FeaturerTwins.TYPE_27,
        name = "SearchCriteriaBuilder",
        description = "")
public abstract class TwinFinder extends SearchFinder<TwinSearch> {
}
