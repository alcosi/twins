package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "TwinflowExportSqlRqV1")
public class TwinflowExportSqlRqDTOv1 extends Request {
    @Schema(description = "twinflow ids to export SQL for")
    public Set<UUID> twinflowIds;

    @Schema(description = "include twinflow factories (links to twin factories) and the factories themselves")
    public boolean includeFactories = false;

    @Schema(description = "include twinflow transitions (with all dependencies)")
    public boolean includeTransitions = false;
}
