package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserGroupInvolveAssigneeUpdateV1")
public class UserGroupInvolveAssigneeUpdateDTOv1 extends UserGroupInvolveAssigneeSaveDTOv1 {
}
