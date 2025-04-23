package org.twins.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.PublicCloneable;

import java.util.ArrayList;
import java.util.List;

/**
 * CUD - create, update, delete
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class EntityCUD<T extends PublicCloneable<T>> extends EntityCD<T> {
    private List<T> updateList;

    public boolean isEmpty() {
        return super.isEmpty() && CollectionUtils.isEmpty(updateList);
    }

    public List<T> getOrInitCreateList() {
        if (this.createList == null) {
            this.createList = new ArrayList<>();
        }
        return this.createList;
    }

    public List<T> getOrInitUpdateList() {
        if (this.updateList == null) {
            this.updateList = new ArrayList<>();
        }
        return this.updateList;
    }

    public List<T> getOrInitDeleteList() {
        if (this.deleteList == null) {
            this.deleteList = new ArrayList<>();
        }
        return this.deleteList;
    }

    @Override
    public EntityCUD<T> clone() {
        EntityCUD<T> clone = new EntityCUD<>();
        clone.createList = cloneCreateList();
        clone.deleteList = cloneDeleteList();
        if (CollectionUtils.isNotEmpty(updateList)) {
            clone.updateList = new ArrayList<>();
            for (T entity : this.updateList) {
                clone.updateList.add(entity.clone());
            }
        }
        return clone;
    }
}
