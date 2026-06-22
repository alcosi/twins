package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@Schema(name = "FactoryPipelineExportSqlRqV1")
public class FactoryPipelineExportSqlRqDTOv1 extends Request {
    @Schema(description = "twin factory pipeline ids to export SQL for")
    public Set<UUID> twinFactoryPipelineIds;

    @Schema(description = "include pipeline steps with condition sets and conditions")
    public boolean includePipelineSteps = false;
}
