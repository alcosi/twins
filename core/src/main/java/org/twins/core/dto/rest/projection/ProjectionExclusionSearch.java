package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class ProjectionExclusionSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> twinIdList;
    private Set<UUID> twinIdExcludeList;
    private Set<UUID> twinClassFieldIdList;
    private Set<UUID> twinClassFieldIdExcludeList;
}
