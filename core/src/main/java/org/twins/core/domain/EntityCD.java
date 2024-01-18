package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.PublicCloneable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * CD - create, delete
 */
@Data
@Accessors(chain = true)
public class EntityCD<T extends PublicCloneable<T>> implements PublicCloneable<EntityCD<T>> {
    protected List<T> createList;
    protected List<UUID> deleteUUIDList;

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(createList) && CollectionUtils.isEmpty(deleteUUIDList);
    }

    @Override
    public EntityCD<T> clone() {
        EntityCD<T> clone = new EntityCD<>();
        clone.createList = cloneCreateList();
        clone.deleteUUIDList = cloneDeleteList();
        return clone;
    }

    protected List<T> cloneCreateList() {
        if (CollectionUtils.isNotEmpty(createList)) {
            List<T> ret = new ArrayList<>();
            for (T entity : this.createList) {
                ret.add(entity.clone());
            }
            return ret;
        }
        return null;
    }

    protected List<UUID> cloneDeleteList() {
        if (CollectionUtils.isNotEmpty(deleteUUIDList)) {
            List<UUID> ret = new ArrayList<>(this.deleteUUIDList);
            return ret;
        }
        return null;
    }
}
