package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionGrantTwinRoleCreateV1")
public class PermissionGrantTwinRoleCreateDTOv1 extends PermissionGrantTwinRoleSaveDTOv1{
}