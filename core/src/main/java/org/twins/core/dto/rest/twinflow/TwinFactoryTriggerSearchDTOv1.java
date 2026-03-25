package org.twins.core.dto.rest.twinflow;

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
@Schema(name = "TwinFactoryTriggerSearchV1")
public class TwinFactoryTriggerSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "twin factory id list")
    public Set<UUID> twinFactoryIdList;

    @Schema(description = "twin factory id exclude list")
    public Set<UUID> twinFactoryIdExcludeList;

    @Schema(description = "input twin class id list")
    public Set<UUID> inputTwinClassIdList;

    @Schema(description = "input twin class id exclude list")
    public Set<UUID> inputTwinClassIdExcludeList;

    @Schema(description = "twin trigger id list")
    public Set<UUID> twinTriggerIdList;

    @Schema(description = "twin trigger id exclude list")
    public Set<UUID> twinTriggerIdExcludeList;

    @Schema(description = "is active")
    public Ternary active;

    @Schema(description = "is async")
    public Ternary async;
}
