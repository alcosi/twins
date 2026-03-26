package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UserGroupInvolveActAsUserV1")
public class UserGroupInvolveActAsUserDTOv1 {
    @Schema(description = "id")
    UUID id;

    @Schema(description = "machine user id")
    @RelatedObject(type = UserDTOv1.class, name = "machineUser")
    UUID machineUserId;

    @Schema(description = "userGroup id)")  //SpaceRoleDTOv1
    @RelatedObject(type = UserGroupDTOv1.class,name = "userGroup")
    UUID userGroupId;
}
