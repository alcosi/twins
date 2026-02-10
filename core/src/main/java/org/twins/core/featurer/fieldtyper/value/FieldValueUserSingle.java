package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.UUID;
import java.util.function.Function;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueUserSingle extends FieldValueItem<UserEntity> {

    public FieldValueUserSingle(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public FieldValueUserSingle setValue(UserEntity newStatus) {
        return (FieldValueUserSingle) super.setValue(newStatus);
    }

    @Override
    public FieldValueUserSingle newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
        return new FieldValueUserSingle(newTwinClassFieldEntity);
    }

    @Override
    protected Function<UserEntity, UUID> itemGetIdFunction() {
        return UserEntity::getId;
    }
}
