package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class ProjectionSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> srcTwinPointerIdList;
    private Set<UUID> srcTwinPointerIdExcludeList;
    private Set<UUID> srcTwinClassFieldIdList;
    private Set<UUID> srcTwinClassFieldIdExcludeList;
    private Set<UUID> dstTwinClassIdList;
    private Set<UUID> dstTwinClassIdExcludeList;
    private Set<UUID> dstTwinClassFieldIdList;
    private Set<UUID> dstTwinClassFieldIdExcludeList;
    private Set<UUID> projectionTypeIdList;
    private Set<UUID> projectionTypeIdExcludeList;
    private Set<Integer> fieldProjectorIdList;
    private Set<Integer> fieldProjectorIdExcludeList;
}
