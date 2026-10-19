package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "UserGroupInvolveActAsUserUpdateV1")
public class UserGroupInvolveActAsUserUpdateDTOv1 {
    @NotNull
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "machine user id")
    public UUID machineUserId;

    @Schema(description = "userGroup id")
    public UUID userGroupId;
}
