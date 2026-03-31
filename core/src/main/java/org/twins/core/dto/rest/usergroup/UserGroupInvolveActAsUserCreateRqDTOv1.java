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
@Schema(name = "UserGroupInvolveActAsUserCreateRqV1")
public class UserGroupInvolveActAsUserCreateRqDTOv1 extends Request {
    @Schema(description = "create(batch) user group involve act as user")
    public List<UserGroupInvolveActAsUserCreateDTOv1> userGroupInvolves;
}
