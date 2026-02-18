package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.Ternary;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinTriggerSearch {
    public Set<UUID> idList;
    public Set<UUID> idExcludeList;
    public Set<Integer> triggerFeaturerIdList;
    public Set<Integer> triggerFeaturerIdExcludeList;
    public Ternary active;
    public Set<String> nameLikeList;
}
