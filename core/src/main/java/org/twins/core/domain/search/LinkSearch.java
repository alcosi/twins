package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.link.LinkStrength;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class LinkSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> srcTwinClassIdList;
    private Set<UUID> srcTwinClassIdExcludeList;
    private Set<UUID> dstTwinClassIdList;
    private Set<UUID> dstTwinClassIdExcludeList;
    private Set<UUID> srcOrDstTwinClassIdList;
    private Set<UUID> srcOrDstTwinClassIdExcludeList;
    private Set<String> forwardNameLikeList;
    private Set<String> forwardNameNotLikeList;
    private Set<String> backwardNameLikeList;
    private Set<String> backwardNameNotLikeList;
    private Set<LinkEntity.TwinlinkType> typeLikeList;
    private Set<LinkEntity.TwinlinkType> typeNotLikeList;
    private Set<LinkStrength> strengthLikeList;
    private Set<LinkStrength> strengthNotLikeList;
}
