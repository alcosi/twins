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
     * Entity behind the group key (UserEntity, TwinStatusEntity, TwinClassEntity, TwinEntity or DataListOptionEntity).
     * Batch-loaded in beforeCollectionConversion(), postponed to relatedObjects in map().
     * map() also loads it for a single-object convert, which skips beforeCollectionConversion.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Object keyEntity;

    /**
     * True after a load attempt for this key, even when the entity was missing.
     * Distinguishes "not loaded yet" from "loaded, absent" so map() does not re-query.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private boolean keyEntityResolved;
}
