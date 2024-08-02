package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.PublicCloneable;

import java.util.ArrayList;
import java.util.List;

/**
 * CD - create, delete
 */
@Data
@Accessors(chain = true)
public class EntityCD<T extends PublicCloneable<T>> implements PublicCloneable<EntityCD<T>> {
    protected List<T> createList;
    protected List<T> deleteList;

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(createList) && CollectionUtils.isEmpty(deleteList);
    }

    @Override
    public EntityCD<T> clone() {
        EntityCD<T> clone = new EntityCD<>();
        clone.createList = cloneCreateList();
        clone.deleteList = cloneDeleteList();
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

    protected List<T> cloneDeleteList() {
        if (CollectionUtils.isNotEmpty(deleteList)) {
            List<T> ret = new ArrayList<>(this.deleteList);
            return ret;
        }
        return null;
    }
}
