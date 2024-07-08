package org.twins.core.dto.rest.attachment;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.comment.CommentBaseDTOv2;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "AttachmentViewV1")
public class AttachmentViewDTOv1 extends AttachmentAddDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = "1549632759")
    public LocalDateTime createdAt;

    @Schema(description = "author id")
    public UUID authorUserId;

    @Schema(description = "author")
    public UserDTOv1 authorUser;

    @Schema(description = "twinflow transition id")
    public UUID twinflowTransitionId;

    @Schema(description = "twinflow transition")
    public TwinflowTransitionBaseDTOv1 twinflowTransition;

    @Schema(description = "comment")
    public CommentBaseDTOv2 comment;

    @Schema(description = "field")
    public TwinClassFieldDTOv1 twinClassField;
}
