package org.twins.core.dto.rest.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.space.SpaceRoleDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "SpaceSearchV1")
public class SpaceSearchDTOv1 {
    @Schema(description = "space id")
    @RelatedObject(type = SpaceRoleDTOv1.class, name = "space")
    public UUID spaceId;

    @Schema(description = "role id")
    @RelatedObject(type = SpaceRoleDTOv1.class, name = "role")
    public UUID roleId;
}


