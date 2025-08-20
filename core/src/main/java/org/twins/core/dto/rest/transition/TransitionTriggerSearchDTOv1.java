package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TransitionTriggerSearchV1")
public class TransitionTriggerSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "twinflow transition id list")
    public Set<UUID> twinflowTransitionIdList;

    @Schema(description = "twinflow transition id exclude list")
    public Set<UUID> twinflowTransitionIdExcludeList;

    @Schema(description = "transition trigger featurer id list")
    public Set<Integer> transitionTriggerFeaturerIdList;

    @Schema(description = "transition trigger featurer id exclude list")
    public Set<Integer> transitionTriggerFeaturerIdExcludeList;
    
    @Schema(description = "active", example = DTOExamples.TERNARY)
    public Ternary active;
}
