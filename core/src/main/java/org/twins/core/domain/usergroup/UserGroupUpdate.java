package org.twins.core.domain.usergroup;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class UserGroupUpdate extends UserGroupSave{
    UUID id;
}
