package org.twins.core.domain.comment;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinCommentEntity;
import org.twins.core.service.pagination.PageableResult;

import java.util.List;

@Data
@Accessors(chain = true)
public class CommentListResult extends PageableResult {
    public List<TwinCommentEntity> commentList;
}
