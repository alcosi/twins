package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "UserGroupInvolveActAsUserSearchV1")
public class UserGroupInvolveActAsUserSearchDTOv1 {

    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "machine user id list")
    public Set<UUID> machineUserIdList;

    @Schema(description = "machine user id exclude list")
    public Set<UUID> machineUserIdExcludeList;

    @Schema(description = "userGroup id list")
    public Set<UUID> userGroupIdList;

    @Schema(description = "userGroup id exclude list")
    public Set<UUID> userGroupIdExcludeList;
}