package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class ProjectionTypeGroupSearch extends EntitySearch<ProjectionTypeGroupEntity> {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<String> keyLikeList;
    private Set<String> keyNotLikeList;
}
