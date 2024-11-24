package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "PermissionGrantSpaceRoleSearchRqV1")
public class PermissionGrantSpaceRoleSearchRqDTOv1 extends Request {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "permission schema id list")
    public Set<UUID> permissionSchemaIdList;

    @Schema(description = "permission schema id exclude list")
    public Set<UUID> permissionSchemaIdExcludeList;

    @Schema(description = "permission id list")
    public Set<UUID> permissionIdList;

    @Schema(description = "permission id exclude list")
    public Set<UUID> permissionIdExcludeList;

    @Schema(description = "space role id list")
    public Set<UUID> spaceRoleIdList;

    @Schema(description = "space role id exclude list")
    public Set<UUID> spaceRoleIdExcludeList;

    @Schema(description = "granted by user id list")
    public Set<UUID> grantedByUserIdList;

    @Schema(description = "granted by user id exclude list")
    public Set<UUID> grantedByUserIdExcludeList;
}
