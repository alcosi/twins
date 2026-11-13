package org.twins.core.featurer.user.sorter;

import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.search.SearchSorter;

@FeaturerType(id = FeaturerTwins.TYPE_42,
        name = "UserSorter",
        description = "Order users search")
public abstract class UserSorter extends SearchSorter<UserEntity> {
}
