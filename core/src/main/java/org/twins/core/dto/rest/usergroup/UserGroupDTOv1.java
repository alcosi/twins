package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "UserGroupV1")
public class UserGroupDTOv1 {
    @Schema(description = "id", example = DTOExamples.SPACE_ROLE_USER_ID)
    public UUID id;

    @Schema(description = "type")
    public String type;

    @Schema(example = DTOExamples.BUSINESS_ACCOUNT_ID)
    public UUID businessAccountId;

    @Schema(description = "name", example = "Manager")
    public String name;

    @Schema(description = "description")
    public String description;

}
