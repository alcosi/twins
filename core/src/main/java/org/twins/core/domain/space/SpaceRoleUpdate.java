package org.twins.core.domain.space;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SpaceRoleUpdate extends SpaceRoleSave {
    private UUID id;
}
