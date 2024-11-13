package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "PermissionSchemaUserGroupV1")
public class PermissionSchemaUserGroupDTOv1 {
    public UUID id;
    public UUID permissionId;
    public UUID userGroupId;
    public UUID grantedByUserId;
    public PermissionDTOv2 permission;
    public UserGroupDTOv1 userGroup;
    public UserDTOv1 grantedByUser;
}
