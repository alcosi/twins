package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionGrantAssigneePropagationUpdateV1")
public class PermissionGrantAssigneePropagationUpdateDTOv1 extends PermissionGrantAssigneePropagationSaveDTOv1 {
}
