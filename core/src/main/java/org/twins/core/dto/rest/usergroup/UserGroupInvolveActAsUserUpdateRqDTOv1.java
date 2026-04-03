package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UserGroupInvolveActAsUserUpdateRqV1")
public class UserGroupInvolveActAsUserUpdateRqDTOv1 extends Request {
    @Schema(description = "usergroup by act as user update(batch)")
    public List<UserGroupInvolveActAsUserUpdateDTOv1> userGroupInvolves;
}
