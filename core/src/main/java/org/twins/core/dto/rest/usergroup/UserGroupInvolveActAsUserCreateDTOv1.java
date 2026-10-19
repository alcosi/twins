package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "UserGroupInvolveActAsUserCreateV1", description = "create(batch) user group involve act as user")
public class UserGroupInvolveActAsUserCreateDTOv1 {
    @NotNull
    @Schema(description = "machine user id")
    public UUID machineUserId;

    @NotNull
    @Schema(description = "userGroup id")
    public UUID userGroupId;
}
