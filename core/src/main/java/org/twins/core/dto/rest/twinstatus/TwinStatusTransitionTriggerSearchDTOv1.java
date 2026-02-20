package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerEntity;

import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinStatusTransitionTriggerSearchV1")
public class TwinStatusTransitionTriggerSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "twin status id list")
    public Set<UUID> twinStatusIdList;

    @Schema(description = "twin status id exclude list")
    public Set<UUID> twinStatusIdExcludeList;

    @Schema(description = "transition type list")
    public Set<TwinStatusTransitionTriggerEntity.TransitionType> typeList;

    @Schema(description = "transition type exclude list")
    public Set<TwinStatusTransitionTriggerEntity.TransitionType> typeExcludeList;

    @Schema(description = "twin trigger id list")
    public Set<UUID> twinTriggerIdList;

    @Schema(description = "twin trigger id exclude list")
    public Set<UUID> twinTriggerIdExcludeList;

    @Schema(description = "is active")
    public Ternary active;

    @Schema(description = "is async")
    public Ternary async;
}
