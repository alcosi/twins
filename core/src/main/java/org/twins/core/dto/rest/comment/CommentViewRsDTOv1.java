package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.twins.core.dto.rest.Response;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "CommentViewRsV1")
public class CommentViewRsDTOv1 extends Response {
    @Schema(description = "comment")
    public CommentViewDTOv1 comment;
}
