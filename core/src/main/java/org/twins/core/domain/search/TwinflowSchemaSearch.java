package org.twins.core.domain.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinflowSchemaSearch {
    Set<UUID> idList;
    Set<UUID> idExcludeList;
    Set<String> nameLikeList;
    Set<String> nameNotLikeList;
    Set<String> descriptionLikeList;
    Set<String> descriptionNotLikeList;
    Set<UUID> businessAccountIdList;
    Set<UUID> businessAccountIdExcludeList;
    Set<UUID> createdByUserIdList;
    Set<UUID> createdByUserIdExcludeList;
}
