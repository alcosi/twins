package org.twins.core.dto.rest.attachment;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.attachment.TwinAttachmentAction;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "AttachmentV1")
public class AttachmentDTOv1 {
    @Schema(description = "id", example = DTOExamples.ATTACHMENT_ID)
    public UUID id;

    @Schema(description = "twin id", example = DTOExamples.TWIN_ID)
    public UUID twinId;

    @Schema(description = "External storage link", example = DTOExamples.ATTACHMENT_STORAGE_LINK)
    public String storageLink;

    @Schema(description = "External storage links map by key", example = DTOExamples.ATTACHMENT_STORAGE_LINKS_MAP)
    public Map<String, String> modifications;

    @Schema(description = "External id", example = DTOExamples.ATTACHMENT_EXTERNAL_ID)
    public String externalId;

    @Schema(description = "Title", example = DTOExamples.ATTACHMENT_TITLE)
    public String title;

    @Schema(description = "Description", example = DTOExamples.ATTACHMENT_TITLE)
    public String description;

    @Schema(description = "File size in bytes", example = DTOExamples.INTEGER)
    public Long size;

    @Schema(description = "view permission id")
    public UUID viewPermissionId;

    @Schema(description = "author id", example = DTOExamples.USER_ID)
    public UUID authorUserId;

    @Schema(description = "comment id", example = DTOExamples.TWIN_COMMENT_ID)
    public UUID commentId;

    @Schema(description = "twin class field id", example = DTOExamples.TWIN_CLASS_FIELD_ID)
    public UUID twinClassFieldId;

    @Schema(description = "twinflow transition id", example = DTOExamples.TWINFLOW_TRANSITION_ID)
    public UUID twinflowTransitionId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "attachment action list")
    public Set<TwinAttachmentAction> attachmentActions;
}
