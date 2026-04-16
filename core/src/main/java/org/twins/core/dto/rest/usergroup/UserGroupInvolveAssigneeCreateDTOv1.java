package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserGroupInvolveAssigneeCreateV1")
public class UserGroupInvolveAssigneeCreateDTOv1 extends UserGroupInvolveAssigneeSaveDTOv1 {
}
