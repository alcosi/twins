package org.twins.core.domain.usergroup;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.user.UserGroupTypeEntity;


@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class UserGroupCreate extends UserGroupSave {

    UserGroupTypeEntity userGroupTypeId;
}
