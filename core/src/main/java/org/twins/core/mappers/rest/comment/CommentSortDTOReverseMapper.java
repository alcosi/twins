package org.twins.core.mappers.rest.comment;

import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.comment.CommentSortDTOv1;
import org.twins.core.enums.sort.CommentSortField;
import org.twins.core.mappers.rest.SortDTOReverseMapper;

@Component
public class CommentSortDTOReverseMapper extends SortDTOReverseMapper<CommentSortDTOv1, CommentSortField> {
    @Override
    protected CommentSortField sortField(CommentSortDTOv1 dto) {
        return dto.getSortField();
    }
}
