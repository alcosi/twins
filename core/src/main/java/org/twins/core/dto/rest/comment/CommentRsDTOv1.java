package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.twins.core.dto.rest.Response;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CommentRsV1")
public class CommentRsDTOv1 extends Response {
    @Schema(description = "comment")
    public CommentDTOv1 comment;
}
