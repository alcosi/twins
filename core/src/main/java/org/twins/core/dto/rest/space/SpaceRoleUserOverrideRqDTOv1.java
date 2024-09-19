package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "SpaceRoleUserOverrideRqV1")
public class SpaceRoleUserOverrideRqDTOv1 {
    @Schema(description = "space role user list")
    public List<UUID> spaceRoleUserList;

}
