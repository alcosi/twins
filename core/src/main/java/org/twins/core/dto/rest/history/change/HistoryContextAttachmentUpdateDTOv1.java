package org.twins.core.dto.rest.history.change;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "HistoryContextAttachmentUpdateV1")
public class HistoryContextAttachmentUpdateDTOv1 implements HistoryContextDTO {
    public static final String KEY = "attachmentUpdateV1";
    public String contextType = KEY;

    @Schema(description = "Attachment id", example = DTOExamples.USER_ID)
    public UUID attachmentId;

    @Schema(description = "From external storage link", example = DTOExamples.ATTACHMENT_STORAGE_LINK)
    public String fromStorageLink;

    @Schema(description = "To external storage link", example = DTOExamples.ATTACHMENT_STORAGE_LINK)
    public String toStorageLink;

    @Schema(description = "From external id", example = DTOExamples.ATTACHMENT_EXTERNAL_ID)
    public String fromExternalId;

    @Schema(description = "To external id", example = DTOExamples.ATTACHMENT_EXTERNAL_ID)
    public String toExternalId;

    @Schema(description = "From title", example = DTOExamples.ATTACHMENT_TITLE)
    public String fromTitle;

    @Schema(description = "To title", example = DTOExamples.ATTACHMENT_TITLE)
    public String toTitle;

    @Schema(description = "From description", example = DTOExamples.ATTACHMENT_TITLE)
    public String fromDescription;

    @Schema(description = "To description", example = DTOExamples.ATTACHMENT_TITLE)
    public String toDescription;
}
