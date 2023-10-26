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
@Schema(name = "PermissionListRsV1")
public class PermissionListRsDTOv1 extends Response {
    @Schema(description = "permission list")
    public List<PermissionWithGroupDTOv1> permissionList;
}
