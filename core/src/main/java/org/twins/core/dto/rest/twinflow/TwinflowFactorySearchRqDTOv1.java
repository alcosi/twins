package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinflowFactorySearchRqV1")
public class TwinflowFactorySearchRqDTOv1 extends Request {

    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "twinflow id list")
    public Set<UUID> twinflowIdList;

    @Schema(description = "twinflow id exclude list")
    public Set<UUID> twinflowIdExcludeList;

    @Schema(description = "twin factory id list")
    public Set<UUID> twinFactoryIdList;

    @Schema(description = "twin factory id exclude list")
    public Set<UUID> twinFactoryIdExcludeList;

    @Schema(description = "factory launcher list")
    public Set<String> factoryLauncherList;

    @Schema(description = "factory launcher exclude list")
    public Set<String> factoryLauncherExcludeList;
}
