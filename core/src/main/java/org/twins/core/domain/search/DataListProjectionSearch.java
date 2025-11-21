package org.twins.core.domain.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.domain.DataTimeRange;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class DataListProjectionSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> srcDataListIdList;
    private Set<UUID> srcDataListIdExcludeList;
    private Set<UUID> dstDataListIdList;
    private Set<UUID> dstDataListIdExcludeList;
    private Set<String> nameLikeList;
    private Set<String> nameNotLikeList;
    private Set<UUID> savedByUserIdList;
    private Set<UUID> savedByUserIdExcludeList;
    private DataTimeRange changedAt;
}
