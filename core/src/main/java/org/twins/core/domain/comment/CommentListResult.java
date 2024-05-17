package org.twins.core.domain.comment;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinCommentEntity;
import org.twins.core.service.pagination.PaginationResult;

import java.util.List;

@Data
@Accessors(chain = true)
public class CommentListResult extends PaginationResult {
    private List<TwinCommentEntity> commentList;
}
