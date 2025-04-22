package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "CommentSaveV1")
public class CommentSaveDTOv1 {
    @Schema(description = "twin id", example = DTOExamples.TWIN_ID)
    public UUID twinId;

    @Schema(name = "text")
    public String text;
}
