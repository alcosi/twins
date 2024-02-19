package org.twins.core.domain.space;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.*;

@Data
@Accessors(chain = true)
public class SpaceRoleUserMap {

    UserEntity user;
    List<SpaceRoleUserEntity> roles;


    public SpaceRoleUserMap addRole(SpaceRoleUserEntity role) {
        roles = safeAdd(roles, role);
        return this;
    }

    public SpaceRoleUserMap addRoles(List<SpaceRoleUserEntity> items) {
        roles = safeAdd(roles, items);
        return this;
    }

    private <T> List<T> safeAdd(List<T> list, T element) {
        if (list == null) list = new ArrayList<>();
        list.add(element);
        return list;
    }

    private <T> List<T> safeAdd(List<T> list, Collection<T> elements) {
        if (list == null) list = new ArrayList<>();
        list.addAll(elements);
        return list;
    }
}
