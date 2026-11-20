package org.twins.core.domain.twin;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.enums.twin.TwinGroupKeyType;

import java.util.List;

@Data
@Accessors(chain = true)
public class TwinGroup {
    private Object key;
    private List<TwinEntity> twins;
    private List<TwinGroup> groups;
    private int count;
    private TwinGroupKeyType keyType;

    /**
     * Entity behind the group key (UserEntity, TwinStatusEntity, TwinClassEntity, TwinEntity or
     * DataListOptionEntity) — an attached relation, same style as TwinEntity.assignerUser. Batch-loaded by
     * TwinGroupedSearchService.loadKeyEntities; the mapper only postpones it into relatedObjects.
     * Null when the key is null, the key type has no entity behind it (TEXT/DECIMAL/BOOLEAN/TIMESTAMP),
     * or the entity is missing / not readable.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Object keyEntity;
}
