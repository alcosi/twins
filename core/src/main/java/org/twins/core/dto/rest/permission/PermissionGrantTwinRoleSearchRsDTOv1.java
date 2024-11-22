package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "PermissionGrantUserGroupSearchRsV1")
public class PermissionGrantTwinRoleSearchRsDTOv1 extends Response {
    @Schema(description = "pagination data")
    public PaginationDTOv1 pagination;

    @Schema(description = "results - permission grant twin role list")
    public List<PermissionGrantTwinRoleDTOv2> permissionGrantTwinRoles;
}
