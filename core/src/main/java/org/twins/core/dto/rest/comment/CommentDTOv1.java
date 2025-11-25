package org.twins.core.dto.rest.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.enums.comment.TwinCommentAction;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "CommentV1")
public class CommentDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_COMMENT_ID)
    public UUID id;

    @Schema(name = "text")
    public String text;

    @Schema(description = "author id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "authorUser")
    public UUID authorUserId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "changed at", example = DTOExamples.INSTANT)
    public LocalDateTime changedAt;

    @Schema(description = "attachment ids")
    public Set<UUID> attachmentIds;

    @Schema(description = "comment actions")
    public Set<TwinCommentAction> commentActions;
}