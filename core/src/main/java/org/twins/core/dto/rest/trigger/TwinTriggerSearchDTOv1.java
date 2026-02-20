package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;

import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinTriggerSearchV1")
public class TwinTriggerSearchDTOv1 {
    @Schema(description = "twin trigger id list")
    public Set<UUID> idList;

    @Schema(description = "twin trigger id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "trigger featurer id list")
    public Set<Integer> triggerFeaturerIdList;

    @Schema(description = "trigger featurer id exclude list")
    public Set<Integer> triggerFeaturerIdExcludeList;

    @Schema(description = "is active")
    public Ternary active;

    @Schema(description = "name like list")
    public Set<String> nameLikeList;
}
