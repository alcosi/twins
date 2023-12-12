package org.twins.core.featurer.fieldtyper.value;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.ArrayList;
import java.util.List;

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
}
