package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionGroupedListRsV1")
public class PermissionGroupedListRsDTOv1 extends Response {
    @Schema(description = "permission groups list")
    public List<PermissionGroupDTOv2> permissionGroups;
}
