package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "SpaceRoleUserRqDTOv1")
public class SpaceRoleUserRqDTOv1 extends Request {
    @Schema()
    public List<UUID> spaceRoleUserEnterList;

    @Schema()
    public List<UUID> spaceRoleUserExitList;
}
