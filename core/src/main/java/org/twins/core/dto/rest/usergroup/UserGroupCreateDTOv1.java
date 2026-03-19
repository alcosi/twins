package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.enums.user.UserGroupType;

@Data
@Accessors(chain = true)
@Schema(name = "UserGroupCreateV1")
@EqualsAndHashCode(callSuper = true)
public class UserGroupCreateDTOv1 extends UserGroupSaveDTOv1 {
    public UserGroupType userGroupTypeId;
}
