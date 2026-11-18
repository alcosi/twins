package org.twins.core.featurer.user.finder;

import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.domain.search.UserSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.search.SearchFinder;

@FeaturerType(id = FeaturerTwins.TYPE_43,
        name = "UserFinder",
        description = "Find users")
public abstract class UserFinder extends SearchFinder<UserSearch> {
}
