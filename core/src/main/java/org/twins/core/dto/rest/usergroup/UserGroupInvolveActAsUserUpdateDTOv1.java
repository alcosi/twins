package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "UserGroupInvolveActAsUserUpdateV1")
@EqualsAndHashCode(callSuper = true)
public class UserGroupInvolveActAsUserUpdateDTOv1 extends UserGroupInvolveActAsUserSaveDTOv1 {
    public UUID id;
}
