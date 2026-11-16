package org.twins.core.domain.search;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dao.comment.TwinCommentSearchEntity;
import org.twins.core.domain.DataTimeRange;

import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class CommentSearch extends EntitySearch<TwinCommentEntity> {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> twinIdList;
    private Set<UUID> twinIdExcludeList;
    private Set<UUID> createdByUserIdList;
    private Set<UUID> createdByUserIdExcludeList;
    private Set<String> textLikeList;
    private Set<String> textNotLikeList;
    private DataTimeRange createdAt;
    private DataTimeRange updatedAt;

    private TwinCommentSearchEntity configuredSearch;

    public static final ImmutableList<Pair<Function<CommentSearch, Set>, BiConsumer<CommentSearch, Set>>> SET_FIELDS = ImmutableList.of(
            Pair.of(CommentSearch::getIdList, CommentSearch::setIdList),
            Pair.of(CommentSearch::getIdExcludeList, CommentSearch::setIdExcludeList),
            Pair.of(CommentSearch::getTwinIdList, CommentSearch::setTwinIdList),
            Pair.of(CommentSearch::getTwinIdExcludeList, CommentSearch::setTwinIdExcludeList),
            Pair.of(CommentSearch::getCreatedByUserIdList, CommentSearch::setCreatedByUserIdList),
            Pair.of(CommentSearch::getCreatedByUserIdExcludeList, CommentSearch::setCreatedByUserIdExcludeList),
            Pair.of(CommentSearch::getTextLikeList, CommentSearch::setTextLikeList),
            Pair.of(CommentSearch::getTextNotLikeList, CommentSearch::setTextNotLikeList)
    );
}
