package org.twins.core.featurer.comment.sorter;

import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.search.SearchSorter;

@FeaturerType(id = FeaturerTwins.TYPE_60,
        name = "CommentSorter",
        description = "Order comments search")
public abstract class CommentSorter extends SearchSorter<TwinCommentEntity> {
}
