package org.twins.core.dto.rest.space;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;

@Data
@Accessors(chain = true)
public class SpaceRoleUserSearch {

    String nameLike;
    Set<UUID> rolesList;


    public SpaceRoleUserSearch addRoleId(UUID roleId) {
        rolesList = safeAdd(rolesList, roleId);
        return this;
    }

    private <T> Set<T> safeAdd(Set<T> set, T element) {
        if (set == null) set = new HashSet<>();
        set.add(element);
        return set;
    }

    private <T> Set<T> safeAdd(Set<T> set, Collection<T> elements) {
        if (set == null) set = new HashSet<>();
        set.addAll(elements);
        return set;
    }
}
