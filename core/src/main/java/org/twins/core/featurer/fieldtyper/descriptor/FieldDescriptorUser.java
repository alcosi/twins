package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldDescriptorUser extends FieldDescriptor {
    private boolean multiple;
    private UUID userFilterId;
    private List<UserEntity> validUsers = new ArrayList<>();

    public FieldDescriptorUser add(UserEntity user) {
        validUsers.add(user);
        return this;
    }
}
