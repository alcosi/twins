package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.DataTimeRange;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@EqualsAndHashCode(callSuper = false)
public class TwinLinkSearch extends EntitySearch<TwinLinkEntity> {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> srcTwinIdList;
    private Set<UUID> srcTwinIdExcludeList;
    private Set<UUID> dstTwinIdList;
    private Set<UUID> dstTwinIdExcludeList;
    private Set<UUID> srcOrDstTwinIdList;
    private Set<UUID> srcOrDstTwinIdExcludeList;
    private Set<UUID> linkIdList;
    private Set<UUID> linkIdExcludeList;
    private Set<UUID> createdByUserIdList;
    private Set<UUID> createdByUserIdExcludeList;
    private DataTimeRange createdAt;
}
