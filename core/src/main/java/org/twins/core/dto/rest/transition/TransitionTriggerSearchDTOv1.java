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

    @Schema(description = "twin trigger id list")
    public Set<UUID> twinTriggerIdList;

    @Schema(description = "twin trigger id exclude list")
    public Set<UUID> twinTriggerIdExcludeList;

    @Schema(description = "active", example = DTOExamples.TERNARY)
    public Ternary active;
}
