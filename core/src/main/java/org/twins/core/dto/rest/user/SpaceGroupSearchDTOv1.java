package org.twins.core.dto.rest.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "SpaceGroupSearchV1")
public class SpaceGroupSearchDTOv1 {
    @Schema(description = "space id")
    public UUID spaceId;

    @Schema(description = "role id")
    public UUID roleId;

    @Schema(description = "user groups id")
    public Set<UUID> userGroupIds;
}
