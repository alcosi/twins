package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.history.HistoryType;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class HistorySearch {
    Set<UUID> idList;
    Set<UUID> idExcludeList;
    Set<UUID> twinIdList;
    Set<UUID> twinIdExcludeList;
    Boolean includeDirectChildren;
    Set<UUID> actorUseridList;
    Set<UUID> actorUserIdExcludeList;
    Set<HistoryType> typeList;
    Set<HistoryType> typeExcludeList;
    DataTimeRangeDTOv1 createdAt;
}
