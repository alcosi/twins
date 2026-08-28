package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class ActionRestrictionReasonSearch extends EntitySearch<ActionRestrictionReasonEntity> {
    public Set<UUID> idList;
    public Set<UUID> idExcludeList;
    public Set<String> typeLikeList;
    public Set<String> typeNotLikeList;
    public Set<String> descriptionLikeList;
    public Set<String> descriptionNotLikeList;
}
