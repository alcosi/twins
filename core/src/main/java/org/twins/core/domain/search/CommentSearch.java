package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class CommentSearch {
    Set<UUID> idList;
    Set<UUID> idExcludeList;
    Set<UUID> twinIdList;
    Set<UUID> twinIdExcludeList;
    Set<UUID> createdByUserIdList;
    Set<UUID> createdByUserIdExcludeList;
    Set<String> textLikeList;
    Set<String> textNotLikeList;
    DataTimeRangeDTOv1 createdAt;
    DataTimeRangeDTOv1 updatedAt;
}
