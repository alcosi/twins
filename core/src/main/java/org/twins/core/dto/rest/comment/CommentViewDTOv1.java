package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.attachment.AttachmentViewDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "CommentViewV1")
public class CommentViewDTOv1 extends CommentBaseDTOv1{
    private UUID id;

    private UUID authorUserId;

    private UserDTOv1 authorUser;

    private Timestamp createdAt;

    private Timestamp changedAt;

    private List<AttachmentViewDTOv1> attachments;
}
