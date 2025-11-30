package org.twins.core.domain.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.domain.DataTimeRange;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class ProjectionTypeSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<String> keyLikeList;
    private Set<String> keyNotLikeList;
    private Set<String> nameLikeList;
    private Set<String> nameNotLikeList;
    private Set<UUID> projectionTypeGroupIdList;
    private Set<UUID> projectionTypeGroupIdExcludeList;
    private Set<UUID> membershipTwinClassIdList;
    private Set<UUID> membershipTwinClassIdExcludeList;
}
