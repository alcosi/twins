package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
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

    @Schema(description = "twin id list")
    public Set<UUID> twinIdList;

    @Schema(description = "twin id exclude list")
    public Set<UUID> twinIdExcludeList;

    @Schema(description = "twin trigger id list")
    public Set<UUID> twinTriggerIdList;

    @Schema(description = "twin trigger id exclude list")
    public Set<UUID> twinTriggerIdExcludeList;

    @Schema(description = "previous twin status id list")
    public Set<UUID> previousTwinStatusIdList;

    @Schema(description = "previous twin status id exclude list")
    public Set<UUID> previousTwinStatusIdExcludeList;

    @Schema(description = "created by user id list")
    public Set<UUID> createdByUserIdList;

    @Schema(description = "created by user id exclude list")
    public Set<UUID> createdByUserIdExcludeList;

    @Schema(description = "business account id list")
    public Set<UUID> businessAccountIdList;

    @Schema(description = "business account id exclude list")
    public Set<UUID> businessAccountIdExcludeList;

    @Schema(description = "status id list")
    public Set<TwinTriggerTaskStatus> statusIdList;

    @Schema(description = "status id exclude list")
    public Set<TwinTriggerTaskStatus> statusIdExcludeList;
}
