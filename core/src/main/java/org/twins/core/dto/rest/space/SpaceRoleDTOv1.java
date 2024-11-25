package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "SpaceRoleV1")
public class SpaceRoleDTOv1 {
    @Schema(description = "space role user id", example = DTOExamples.SPACE_ROLE_USER_ID)
    public UUID id;

    @Schema(description = "key", example = "Member")
    public String key;

    @Schema(description = "name", example = DTOExamples.NAME)
    public String name;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;

    @Schema(description = "business account id", example = DTOExamples.BUSINESS_ACCOUNT_ID)
    public UUID businessAccountId;
}
