package org.twins.core.featurer.comment.finder;

import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.domain.search.CommentSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.search.SearchFinder;

@FeaturerType(id = FeaturerTwins.TYPE_59,
        name = "CommentFinder",
        description = "Find comments")
public abstract class CommentFinder extends SearchFinder<CommentSearch> {
    public static final String PARAM_CURRENT_TWIN_ID = "twinId";
}
