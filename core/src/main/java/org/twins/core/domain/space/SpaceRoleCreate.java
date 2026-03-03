package org.twins.core.domain.space;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SpaceRoleCreate extends SpaceRoleSave {
}
