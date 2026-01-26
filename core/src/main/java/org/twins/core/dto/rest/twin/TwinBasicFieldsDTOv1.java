package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinBasicFieldsV1")
public class TwinBasicFieldsDTOv1 {

    @Schema(description = "assignee user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "assigneeUser")
    public UUID assigneeUserId;

    @Schema(description = "created by user_id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "createdByUser")
    public UUID createdByUserId;

    @Schema(name = "name")
    public String name;

    @Schema(name = "description")
    public String description;

}


