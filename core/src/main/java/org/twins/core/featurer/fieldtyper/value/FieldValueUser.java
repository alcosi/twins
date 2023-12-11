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
@Accessors(fluent = true)
public class FieldValueUser extends FieldValue {
    private List<UserEntity> users = new ArrayList<>();

    public FieldValueUser add(UserEntity user) {
        users.add(user);
        return this;
    }
}
