package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueUser extends FieldValueCollection<UserEntity> {
    private List<UserEntity> users = null;

    public FieldValueUser(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    public FieldValueUser setUsers(List<UserEntity> newUsers) {
        users = setWithNullifyMarkerSupport(newUsers);
        return this;
    }

    @Override
    protected List<UserEntity> getCollection() {
        return users;
    }

    @Override
    protected Function<UserEntity, UUID> itemGetIdFunction() {
        return UserEntity::getId;
    }

    public FieldValueUser add(UserEntity user) {
        users = addWithNullifyMarkerSupport(users, user);
        return this;
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueUser clone = new FieldValueUser(newTwinClassFieldEntity);
        clone.setUsers(this.getItems()); // we have to copy a list
        return clone;
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        users.clear();
        users.addAll(((FieldValueUser) src).users);
    }

    @Override
    public void onUndefine() {
        users = null;
    }

    @Override
    public void onClear() {
        users.clear();
    }
}
