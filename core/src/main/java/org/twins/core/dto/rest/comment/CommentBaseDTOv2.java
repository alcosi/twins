package org.twins.core.dto.rest.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "CommentBaseDTOv2")
public class CommentBaseDTOv2 extends CommentBaseDTOv1 {

    @Schema(description = "id", example = DTOExamples.TWIN_COMMENT_ID)
    public UUID id;

    @Schema(description = "author id", example = DTOExamples.USER_ID)
    public UUID authorUserId;

    @Schema(description = "current author")
    public UserDTOv1 authorUser;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "changed at", example = DTOExamples.INSTANT)
    public LocalDateTime changedAt;

}
