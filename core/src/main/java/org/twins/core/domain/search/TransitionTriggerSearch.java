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
public class TransitionTriggerSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> twinflowTransitionIdList;
    private Set<UUID> twinflowTransitionIdExcludeList;
    private Set<Integer> transitionTriggerFeaturerIdList;
    private Set<Integer> transitionTriggerFeaturerIdExcludeList;
    private Ternary active;
}
