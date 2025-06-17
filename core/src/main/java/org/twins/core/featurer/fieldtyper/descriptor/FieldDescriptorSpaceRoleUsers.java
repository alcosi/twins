package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.user.UserEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldDescriptorSpaceRoleUsers extends FieldDescriptor {
    private UUID userFilterId;
    private UUID spaceRoleId;
    private List<UserEntity> validUsers = new ArrayList<>();

    public FieldDescriptorSpaceRoleUsers add(UserEntity user) {
        validUsers.add(user);
        return this;
    }
}
