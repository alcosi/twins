package org.twins.core.dto.rest.attachment;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.attachment.TwinAttachmentAction;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AttachmentV1")
public class AttachmentDTOv1 extends AttachmentBaseDTOv1 {
    @Schema(description = "id", example = DTOExamples.ATTACHMENT_ID)
    public UUID id;

    @Schema(description = "author id", example = DTOExamples.USER_ID)
    public UUID authorUserId;

    @Schema(description = "comment id", example = DTOExamples.TWIN_COMMENT_ID)
    public UUID commentId;

    @Schema(description = "twin class field id", example = DTOExamples.TWIN_CLASS_FIELD_ID)
    public UUID twinClassFieldId;

    @Schema(description = "twinflow transition id")
    public UUID twinflowTransitionId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "attachment action list")
    public Set<TwinAttachmentAction> attachmentActions;
}
