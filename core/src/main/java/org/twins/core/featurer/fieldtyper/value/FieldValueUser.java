package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueUser extends FieldValue {
    private List<UserEntity> users = new ArrayList<>();

    public FieldValueUser(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public boolean isFilled() {
        return CollectionUtils.isNotEmpty(users);
    }

    public FieldValueUser add(UserEntity user) {
        users.add(user);
        return this;
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueUser clone = new FieldValueUser(newTwinClassFieldEntity);
        clone.getUsers().addAll(this.getUsers()); // we have to copy list
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        if (CollectionUtils.isEmpty(users)) {
            return false;
        }
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

    @Override
    public void copyValueFrom(FieldValue src) {
        users.clear();
        users.addAll(((FieldValueUser) src).users);
    }

    @Override
    public void nullify() {
        users = new ArrayList<>();
    }

    @Override
    public boolean isNullified() {
        return users != null && users.isEmpty();
    }
}
