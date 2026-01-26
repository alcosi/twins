package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "SpaceRoleListRsv1")
public class SpaceRoleListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "space role user list")
    public List<SpaceRoleDTOv1> spaceRoleUserList;
}
