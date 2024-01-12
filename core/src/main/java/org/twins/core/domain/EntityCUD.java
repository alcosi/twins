package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.PublicCloneable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * CUD - create, update, delete
 */
@Data
@Accessors(chain = true)
public class EntityCUD<T extends PublicCloneable<T>> implements PublicCloneable<EntityCUD<T>> {
    private List<T> createList;
    private List<T> updateList;
    private List<UUID> deleteUUIDList;

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(createList) && CollectionUtils.isEmpty(updateList) && CollectionUtils.isEmpty(deleteUUIDList);
    }

    @Override
    public EntityCUD<T> clone() {
        EntityCUD<T> clone = new EntityCUD<>();
        if (CollectionUtils.isNotEmpty(createList)) {
            clone.createList = new ArrayList<>();
            for (T entity : this.createList) {
                clone.createList.add(entity.clone());
            }
        }
        if (CollectionUtils.isNotEmpty(updateList)) {
            clone.updateList = new ArrayList<>();
            for (T entity : this.updateList) {
                clone.updateList.add(entity.clone());
            }
        }
        if (CollectionUtils.isNotEmpty(deleteUUIDList)) {
            clone.deleteUUIDList = new ArrayList<>();
            clone.deleteUUIDList.addAll(this.deleteUUIDList);
        }
        return clone;
    }
}
