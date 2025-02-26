package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionGrantAssigneePropagationV2")
public class PermissionGrantAssigneePropagationDTOv2 extends PermissionGrantAssigneePropagationDTOv1 {
    @Schema(description = "permission schema")
    public PermissionSchemaDTOv1 permissionSchema;

    @Schema(description = "permission")
    public PermissionDTOv1 permission;

    @Schema(description = "propagation twin class")
    public TwinClassDTOv1 propagationTwinClass;

    @Schema(description = "propagation twin status")
    public TwinStatusDTOv1 propagationTwinStatus;

    @Schema(description = "granted by user")
    public UserDTOv1 grantedByUser;
}
