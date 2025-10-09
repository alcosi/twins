package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twinclass.TwinClassBaseDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "SpaceRoleUserV1")
public class SpaceRoleUserDTOv1 {
    @Schema(description = "id", example = DTOExamples.SPACE_ROLE_USER_ID)
    public UUID id;

    @Schema(description = "twin id", example = DTOExamples.TWIN_ID)
    @RelatedObject(type = TwinClassBaseDTOv1.class, name = "twin")
    public UUID twinId;

    @Schema(description = "space role id", example = DTOExamples.SPACE_ROLE)
    @RelatedObject(type = SpaceRoleDTOv1.class, name = "spaceRole")
    public UUID spaceRoleId;

    @Schema(description = "user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserGroupDTOv1.class, name = "user")
    public UUID userId;

    @Schema(description = "created by user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "createdByUser")
    public UUID createdByUserId;

    @Schema(description = "space role")
    public SpaceRoleDTOv2 spaceRole;

}


