package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UserGroupInvolveActAsUserSaveV1")
public class UserGroupInvolveActAsUserSaveDTOv1 {
    @Schema(description = "machine user id")
    UUID machineUserId;

    @Schema(description = "userGroup id")
    UUID userGroupId;
}
