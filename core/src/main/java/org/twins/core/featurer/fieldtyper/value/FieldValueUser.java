package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.UUID;
import java.util.function.Function;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueUser extends FieldValueCollectionImmutable<UserEntity> {
    public FieldValueUser(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    protected Function<UserEntity, UUID> itemGetIdFunction() {
        return UserEntity::getId;
    }

    @Override
    public FieldValueUser clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueUser clone = new FieldValueUser(newTwinClassFieldEntity);
        clone.setItems(this.collection); // we have to copy a list
        return clone;
    }
}
