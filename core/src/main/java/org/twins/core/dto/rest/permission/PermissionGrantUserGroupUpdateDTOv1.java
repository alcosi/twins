package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionGrantUserGroupUpdateV1")
public class PermissionGrantUserGroupUpdateDTOv1 extends PermissionGrantUserGroupSaveDTOv1 {
}
