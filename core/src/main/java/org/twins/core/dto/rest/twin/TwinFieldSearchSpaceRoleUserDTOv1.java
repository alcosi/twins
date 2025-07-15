package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name = TwinFieldSearchSpaceRoleUserDTOv1.KEY)
public class TwinFieldSearchSpaceRoleUserDTOv1 implements TwinFieldSearchDTOv1 {
    public static final String KEY = "TwinFieldSearchSpaceRoleUserV1";

    public TwinFieldSearchSpaceRoleUserDTOv1() {
        this.type = KEY;
    }

    @Schema(description = "Search type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String type;

    @Schema(description = "Role id list")
    public Set<UUID> roleIdList;

    @Schema(description = "Role id exclude list")
    public Set<UUID> roleIdExcludeList;

    @Schema(description = "User id list")
    public Set<UUID> userIdList;

    @Schema(description = "User id exclude list")
    public Set<UUID> userIdExcludeList;
}
