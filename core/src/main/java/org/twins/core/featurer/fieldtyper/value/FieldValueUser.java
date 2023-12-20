package org.twins.core.featurer.fieldtyper.value;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.user.UserEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueUser extends FieldValue {
    private List<UserEntity> users = new ArrayList<>();

    public FieldValueUser add(UserEntity user) {
        users.add(user);
        return this;
    }

    @Override
    public FieldValue clone() {
        FieldValueUser clone = new FieldValueUser();
        clone
                .setTwinClassField(this.getTwinClassField());
        clone.getUsers().addAll(this.getUsers()); // we have to copy list
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        UUID valueUUID;
        try {
            valueUUID = UUID.fromString(value);
        } catch (Exception e) {
            return false;
        }
        for (UserEntity userEntity : users) {
            if (userEntity.getId() != null && userEntity.getId().equals(valueUUID))
                return true;
        }
        return false;
    }
}
