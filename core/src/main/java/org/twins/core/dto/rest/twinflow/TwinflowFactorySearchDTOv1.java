package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinflowFactorySearchV1")
public class TwinflowFactorySearchDTOv1 {

    @Schema(description = "id set")
    public Set<UUID> idSet;

    @Schema(description = "id exclude set")
    public Set<UUID> idExcludeSet;

    @Schema(description = "twinflow id set")
    public Set<UUID> twinflowIdSet;

    @Schema(description = "twinflow id exclude set")
    public Set<UUID> twinflowIdExcludeSet;

    @Schema(description = "twin factory id set")
    public Set<UUID> factoryIdSet;

    @Schema(description = "twin factory id exclude set")
    public Set<UUID> factoryIdExcludeSet;

    @Schema(description = "factory launcher set")
    public Set<String> factoryLauncherSet;

    @Schema(description = "factory launcher exclude set")
    public Set<String> factoryLauncherExcludeSet;
}
