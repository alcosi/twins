package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "SpaceRoleUpdateV1")
public class SpaceRoleUpdateDTOv1 extends SpaceRoleSaveDTOv1 {
    @Schema(description = "space role id")
    public UUID id;
}
