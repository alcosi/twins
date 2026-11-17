package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dao.comment.TwinCommentSearchEntity;
import org.twins.core.domain.DataTimeRange;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class CommentSearch extends EntitySearch<TwinCommentEntity> implements ConfiguredSearchHolder<TwinCommentSearchEntity> {
    @SearchFilter
    private Set<UUID> idList;

    @SearchFilter
    private Set<UUID> idExcludeList;

    @SearchFilter
    private Set<UUID> twinIdList;

    @SearchFilter
    private Set<UUID> twinIdExcludeList;

    @SearchFilter
    private Set<UUID> createdByUserIdList;

    @SearchFilter
    private Set<UUID> createdByUserIdExcludeList;

    @SearchFilter
    private Set<String> textLikeList;

    @SearchFilter
    private Set<String> textNotLikeList;

    private DataTimeRange createdAt;
    private DataTimeRange updatedAt;
    private TwinCommentSearchEntity configuredSearch;
    private boolean emptySearch = false;
}
