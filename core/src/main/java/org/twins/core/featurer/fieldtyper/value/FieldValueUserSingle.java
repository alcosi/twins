package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueUserSingle extends FieldValue {
    private UserEntity user;

    public FieldValueUserSingle(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public boolean isFilled() {
        return !ObjectUtils.isEmpty(user);
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueUserSingle clone = new FieldValueUserSingle(newTwinClassFieldEntity);
        clone.setUser(this.getUser());
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
        return user.getId() != null && user.getId().equals(valueUUID);
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        user = ((FieldValueUserSingle) src).user;
    }

    @Override
    public void nullify() {
        user = null;
    }

    @Override
    public boolean isNullified() {
        return user == null;
    }
}
