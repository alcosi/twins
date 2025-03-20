package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "CommentCreateRsV1")
public class CommentCreateRsDTOv1 extends Response {
    @Schema(description = "comment id")
    public UUID commentId;
    @Schema(description = "attachment list id")
    public List<UUID> attachmentListId;
}
