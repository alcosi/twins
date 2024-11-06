package org.twins.core.dto.rest.permission;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "PermissionSchemaUserGroupSearchRqV1")
public class PermissionSchemaUserGroupSearchRqDTOv1 extends Request {

    @JsonProperty("idList")
    @Schema(description = "id list")
    public Set<UUID> idList;

    @JsonProperty("idExcludeList")
    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @JsonProperty("permissionSchemaIdList")
    @Schema(description = "permission schema id list")
    public Set<UUID> permissionSchemaIdList;

    @JsonProperty("permissionSchemaIdExcludeList")
    @Schema(description = "permission schema id exclude list")
    public Set<UUID> permissionSchemaIdExcludeList;

    @JsonProperty("permissionIdList")
    @Schema(description = "permission id list")
    public Set<UUID> permissionIdList;

    @JsonProperty("permissionIdExcludeList")
    @Schema(description = "permission id exclude list")
    public Set<UUID> permissionIdExcludeList;

    @JsonProperty("userGroupIdList")
    @Schema(description = "user group id list")
    public Set<UUID> userGroupIdList;

    @JsonProperty("userGroupIdExcludeList")
    @Schema(description = "user group id exclude list")
    public Set<UUID> userGroupIdExcludeList;

    @JsonProperty("grantedByUserIdList")
    @Schema(description = "granted by user id list")
    public Set<UUID> grantedByUserIdList;

    @JsonProperty("grantedByUserIdExcludeList")
    @Schema(description = "granted by user id exclude list")
    public Set<UUID> grantedByUserIdExcludeList;
}
