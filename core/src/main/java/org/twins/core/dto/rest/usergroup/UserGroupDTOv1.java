package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.enums.user.UserGroupType;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "UserGroupV1")
public class UserGroupDTOv1 {
    @Schema(description = "id", example = DTOExamples.USER_GROUP_ID)
    public UUID id;

    @Schema(example = DTOExamples.BUSINESS_ACCOUNT_ID)
    @RelatedObject(type = BusinessAccountDTOv1.class, name = "businessAccount")
    public UUID businessAccountId;

    @Schema(description = "name", example = DTOExamples.USER_GROUP_NAME)
    public String name;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @Schema(description = "type", example = DTOExamples.USER_GROUP_TYPE)
    public UserGroupType type;
}


