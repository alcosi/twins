package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;


@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CommentCreateRqV1")
public class CommentCreateRqDTOv1 extends Request {
    @Schema(description = "comment")
    public CommentCreateDTOv1 comment;
}
