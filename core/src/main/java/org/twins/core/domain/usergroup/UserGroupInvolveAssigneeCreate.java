package org.twins.core.domain.usergroup;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class UserGroupInvolveAssigneeCreate extends UserGroupInvolveAssigneeSave {
}
