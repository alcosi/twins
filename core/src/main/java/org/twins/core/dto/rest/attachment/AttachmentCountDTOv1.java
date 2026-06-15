package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.comment.CommentDTOv1;
import org.twins.core.dto.rest.permission.PermissionDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "AttachmentCountV1")
public class AttachmentCountDTOv1 extends CountDTOv1 {
    @Schema(description = "twin id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "twin")
    public UUID twinId;

    @Schema(description = "twinflow transition id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = TwinflowTransitionBaseDTOv1.class, name = "twinflowTransition")
    public UUID twinflowTransitionId;

    @Schema(description = "view permission id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = PermissionDTOv1.class, name = "viewPermission")
    public UUID viewPermissionId;

    @Schema(description = "created by user id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = UserDTOv1.class, name = "authorUser")
    public UUID createdByUserId;

    @Schema(description = "comment id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = CommentDTOv1.class, name = "comment")
    public UUID twinCommentId;

    @Schema(description = "twin class field id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = TwinClassFieldDTOv1.class, name = "twinClassField")
    public UUID twinClassFieldId;

    @Schema(description = "storage id", example = DTOExamples.UUID_ID)
    public UUID storageId;
}
