package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "SpaceRoleV1")
public class SpaceRoleDTOv1 {
    @Schema(description = "space role user id", example = DTOExamples.SPACE_ROLE_USER_ID)
    public UUID id;

    @Schema(description = "key", example = "Member")
    public String key;

    @Schema(description = "description", example = "Member i18n description")
    public String description;

    @Schema(description = "name", example = "Member i18n name")
    public String name;

}
