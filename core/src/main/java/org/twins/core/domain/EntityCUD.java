package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.UUID;

/**
 * CUD - create, update, delete
 */
@Data
@Accessors(chain = true)
public class EntityCUD<T>  {
    private List<T> createList;
    private List<T> updateList;
    private List<UUID> deleteUUIDList;

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(createList) && CollectionUtils.isEmpty(updateList) && CollectionUtils.isEmpty(deleteUUIDList);
    }

}
