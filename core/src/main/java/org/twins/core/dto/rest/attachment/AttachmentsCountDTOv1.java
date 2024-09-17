package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "AttachmentsCountV1")
public class AttachmentsCountDTOv1 {
    @Schema(description = "Total number of attachments", example = "20")
    public int all;

    @Schema(description = "Number of attachments direct for a twin", example = "12")
    public int direct;

    @Schema(description = "Number of attachments for transition only", example = "3")
    public int fromTransitions;

    @Schema(description = "Number of attachments for comment only", example = "4")
    public int fromComments;

    @Schema(description = "Number of attachments for twin class field only", example = "1")
    public int fromFields;
}
