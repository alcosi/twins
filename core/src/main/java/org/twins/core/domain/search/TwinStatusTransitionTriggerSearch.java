package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerEntity;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinStatusTransitionTriggerSearch {
    public Set<UUID> idList;
    public Set<UUID> idExcludeList;
    public Set<UUID> twinStatusIdList;
    public Set<UUID> twinStatusIdExcludeList;
    public Set<TwinStatusTransitionTriggerEntity.TransitionType> typeList;
    public Set<TwinStatusTransitionTriggerEntity.TransitionType> typeExcludeList;
    public Set<UUID> twinTriggerIdList;
    public Set<UUID> twinTriggerIdExcludeList;
    public Ternary active;
    public Ternary async;
}
