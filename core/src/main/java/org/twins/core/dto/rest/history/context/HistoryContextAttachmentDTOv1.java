package org.twins.core.dto.rest.history.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.attachment.AttachmentDTOv1;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  HistoryContextAttachmentDTOv1.KEY)
public class HistoryContextAttachmentDTOv1 implements HistoryContextDTO {

    public static final String KEY = "HistoryContextAttachmentV1";

    public HistoryContextAttachmentDTOv1() {
        this.contextType = KEY;
    }

    @Schema(description = "Context type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String contextType;

    @Schema(description = "Attachment id", example = DTOExamples.ATTACHMENT_ID)
    public UUID attachmentId;

    @Schema(description = "Attachment")
    public AttachmentDTOv1 attachment;
}
