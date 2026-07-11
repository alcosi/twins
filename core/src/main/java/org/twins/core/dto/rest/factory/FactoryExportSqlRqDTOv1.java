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
@Schema(name = "TwinFactoryExportSqlRqV1")
public class FactoryExportSqlRqDTOv1 extends Request {
    @Schema(description = "twin factory ids to export SQL for")
    public Set<UUID> twinFactoryIds;

    @Schema(description = "include condition sets")
    public boolean includeConditionSets = false;

    @Schema(description = "include branches with condition sets and conditions")
    public boolean includeBranches = false;

    @Schema(description = "include multipliers with filters, condition sets and conditions")
    public boolean includeMultipliers = false;

    @Schema(description = "include pipelines with condition sets and conditions")
    public boolean includePipelines = false;

    @Schema(description = "include pipeline steps with condition sets and conditions (only works if includePipelines is true)")
    public boolean includePipelineSteps = false;

    @Schema(description = "include erasers with condition sets and conditions")
    public boolean includeErasers = false;

    @Schema(description = "include triggers with condition sets, conditions")
    public boolean includeTriggers = false;
}
