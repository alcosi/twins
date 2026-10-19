package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "CommentCountV1")
public class CommentCountDTOv1 extends CountDTOv1 {
    @Schema(description = "twin id", example = DTOExamples.TWIN_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "twin")
    public UUID twinId;

    @Schema(description = "author user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "authorUser")
    public UUID createdByUserId;
}
