package org.twins.core.dto.rest.history.change;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.attachment.AttachmentViewDTOv1;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "HistoryContextAttachmentV1")
public class HistoryContextAttachmentDTOv1 implements HistoryContextDTO {
    public static final String KEY = "attachmentV1";
    public String contextType = KEY;

    @Schema(description = "Attachment id", example = DTOExamples.USER_ID)
    public UUID attachmentId;

    @Schema(description = "Attachment")
    public AttachmentViewDTOv1 attachment;
}
