package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionGrantUserGroupCreateRsV1")
public class PermissionGrantUserGroupCreateRsDTOv1 extends PermissionGrantUserGroupSaveRsDTOv1 {
}
