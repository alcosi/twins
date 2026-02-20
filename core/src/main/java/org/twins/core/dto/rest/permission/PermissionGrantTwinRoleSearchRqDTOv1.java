package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.enums.twin.TwinRole;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "PermissionGrantTwinRoleSearchRqV1")
public class PermissionGrantTwinRoleSearchRqDTOv1 extends Request {

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

    @Schema(description = "twin class id list")
    public Set<UUID> twinClassIdList;

    @Schema(description = "twin class id exclude list")
    public Set<UUID> twinClassIdExcludeList;

    @Schema(description = "is assignee", example = DTOExamples.TERNARY)
    public Ternary isAssignee;

    @Schema(description = "is space assignee", example = DTOExamples.TERNARY)
    public Ternary isSpaceAssignee;

    @Schema(description = "is creator", example = DTOExamples.TERNARY)
    public Ternary isCreator;

    @Schema(description = "is space creator", example = DTOExamples.TERNARY)
    public Ternary isSpaceCreator;

    @Schema(description = "granted by user id list")
    public Set<UUID> grantedByUserIdList;

    @Schema(description = "granted by user id exclude list")
    public Set<UUID> grantedByUserIdExcludeList;
}
