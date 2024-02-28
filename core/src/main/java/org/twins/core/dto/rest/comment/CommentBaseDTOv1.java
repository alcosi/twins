package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.twins.core.dto.rest.Request;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "CommentBaseDTOv1")
public class CommentBaseDTOv1 extends Request {
    @Schema(name = "text")
    public String text;
}
