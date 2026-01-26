package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "SpaceRoleUserGroupV1")
public class SpaceRoleUserGroupDTOv1 extends Request {

    @Schema(description = "")
    public UUID id;

    @Schema(description = "")
    @RelatedObject(type = TwinDTOv2.class, name = "twin")
    public UUID twinId;

    @Schema(description = "")
    @RelatedObject(type = SpaceRoleDTOv1.class, name = "spaceRole")
    public UUID spaceRoleId;

    @Schema(description = "")
    @RelatedObject(type = UserGroupDTOv1.class, name = "userGroup")
    public UUID userGroupId;

    @Schema(description = "")
    @RelatedObject(type = UserDTOv1.class, name = "createdByUser")
    public UUID createdByUserId;
}


