package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain=true)
public class FieldProjectionSearch {
    private ProjectionFieldSelector projectionFieldSelector;
    private Set<UUID> srcIdList;
    private Set<UUID> dstIdList;
    private Set<UUID> projectionTypeIdList;

    public static enum ProjectionFieldSelector {
        src,
        dst,
        all;
    }
}
