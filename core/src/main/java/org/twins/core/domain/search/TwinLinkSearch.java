package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinLinkSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> srcTwinIdList;
    private Set<UUID> srcTwinIdExcludeList;
    private Set<UUID> dstTwinIdList;
    private Set<UUID> dstTwinIdExcludeList;
    private Set<UUID> linkIdList;
    private Set<UUID> linkIdExcludeList;
}
