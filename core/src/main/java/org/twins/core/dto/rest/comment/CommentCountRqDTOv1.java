package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.CommentGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CommentCountRqV1")
public class CommentCountRqDTOv1 extends Request {
    @Schema(description = "search params")
    public CommentSearchDTO search;

    @Schema(description = "Group by fields")
    public Set<CommentGroupField> groupFields;
}
