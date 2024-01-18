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

    @Override
    public EntityCUD<T> clone() {
        EntityCUD<T> clone = new EntityCUD<>();
        clone.createList = cloneCreateList();
        clone.deleteUUIDList = cloneDeleteList();
        if (CollectionUtils.isNotEmpty(updateList)) {
            clone.updateList = new ArrayList<>();
            for (T entity : this.updateList) {
                clone.updateList.add(entity.clone());
            }
        }
        return clone;
    }
}
