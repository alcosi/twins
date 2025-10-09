package org.twins.core.dto.rest.history.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.attachment.AttachmentDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentRestrictionDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "HistoryContextAttachmentV1")
public class HistoryContextAttachmentDTOv1 implements HistoryContextDTO {
    public static final String KEY = "attachmentV1";
    public String contextType = KEY;

    @Schema(description = "Attachment id", example = DTOExamples.ATTACHMENT_ID)
    @RelatedObject(type = AttachmentRestrictionDTOv1.class, name = "attachment")
    public UUID attachmentId;

    @Schema(description = "Attachment")
    public AttachmentDTOv1 attachment;
}


