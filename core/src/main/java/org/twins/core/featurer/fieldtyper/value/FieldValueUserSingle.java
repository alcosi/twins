package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.UUID;
import java.util.function.Function;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueUserSingle extends FieldValueItem<UserEntity> {
    @Getter
    private UserEntity user;

    public FieldValueUserSingle(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    public FieldValueUserSingle setUser(UserEntity newUser) {
        user = setItemWithNullifSupport(newUser);
        return this;
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueUserSingle clone = new FieldValueUserSingle(newTwinClassFieldEntity);
        clone.setUser(this.getUser());
        return clone;
    }

    @Override
    protected UserEntity getItem() {
        return user;
    }

    @Override
    protected Function<UserEntity, UUID> itemGetIdFunction() {
        return UserEntity::getId;
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        user = ((FieldValueUserSingle) src).user;
    }

    @Override
    public void onUndefine() {
        user = null;
    }

    @Override
    public void onClear() {
        user = null;
    }
}
