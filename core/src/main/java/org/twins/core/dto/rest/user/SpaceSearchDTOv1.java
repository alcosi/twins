package org.twins.core.dto.rest.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "SpaceSearchV1")
public class SpaceSearchDTOv1 {
    @Schema(description = "space id")
    public UUID spaceId;

    @Schema(description = "role id")
    public UUID roleId;
}


