package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.trigger.TwinTriggerTaskStatus;

import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinTriggerTaskSearchV1")
public class TwinTriggerTaskSearchDTOv1 {
    @Schema(description = "twin trigger task id list")
    public Set<UUID> idList;

    @Schema(description = "twin trigger task id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "")
    public Set<UUID> twinIdList;

    @Schema(description = "")
    public Set<UUID> twinIdExcludeList;

    @Schema(description = "")
    public Set<UUID> twinTriggerIdList;

    @Schema(description = "")
    public Set<UUID> twinTriggerIdExcludeList;

    @Schema(description = "")
    public Set<UUID> previousTwinStatusIdList;

    @Schema(description = "")
    public Set<UUID> previousTwinStatusIdExcludeList;

    @Schema(description = "")
    public Set<UUID> createdByUserIdList;

    @Schema(description = "")
    public Set<UUID> createdByUserIdExcludeList;

    @Schema(description = "")
    public Set<UUID> businessAccountIdList;

    @Schema(description = "")
    public Set<UUID> businessAccountIdExcludeList;

    @Schema(description = "")
    public Set<TwinTriggerTaskStatus> statusIdList;

    @Schema(description = "")
    public Set<TwinTriggerTaskStatus> statusIdExcludeList;


//    @Schema(description = "twin trigger id list")
//    public Set<UUID> idList;
//
//    @Schema(description = "twin trigger id exclude list")
//    public Set<UUID> idExcludeList;
//
//    @Schema(description = "trigger featurer id list")
//    public Set<Integer> triggerFeaturerIdList;
//
//    @Schema(description = "trigger featurer id exclude list")
//    public Set<Integer> triggerFeaturerIdExcludeList;
//
//    @Schema(description = "is active")
//    public Ternary active;
//
//    @Schema(description = "name like list")
//    public Set<String> nameLikeList;
}
