package org.twins.core.dto.rest.usergroup;

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
@Schema(name =  "UserGroupByAssigneePropagationSearchRqV1")
public class UserGroupByAssigneePropagationSearchRqDTOv1 extends Request {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "permission schema id list")
    public Set<UUID> permissionSchemaIdList;

    @Schema(description = "permission schema id exclude list")
    public Set<UUID> permissionSchemaIdExcludeList;

    @Schema(description = "user group id list")
    public Set<UUID> userGroupIdList;

    @Schema(description = "user group id exclude list")
    public Set<UUID> userGroupIdExcludeList;

    @Schema(description = "propagation twin class id list")
    public Set<UUID> propagationTwinClassIdList;

    @Schema(description = "propagation twin class id exclude list")
    public Set<UUID> propagationTwinClassIdExcludeList;

    @Schema(description = "propagation twin status id list")
    public Set<UUID> propagationTwinStatusIdList;

    @Schema(description = "propagation twin status id exclude list")
    public Set<UUID> propagationTwinStatusIdExcludeList;

    @Schema(description = "created by user id list")
    public Set<UUID> createdByUserIdList;

    @Schema(description = "created by user id exclude list")
    public Set<UUID> createdByUserIdExcludeList;
}
