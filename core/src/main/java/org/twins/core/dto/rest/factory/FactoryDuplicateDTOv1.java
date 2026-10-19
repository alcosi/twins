package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryDuplicateV1")
public class FactoryDuplicateDTOv1 {
    @Schema(description = "original factory id")
    public UUID originalFactoryId;

    @Schema(description = "new factory key", example = "PROJECT_COPY")
    public String newKey;

    @Schema(description = "[optional] duplicate branches with condition sets and conditions")
    public boolean duplicateBranches = false;

    @Schema(description = "[optional] duplicate multipliers with filters, condition sets and conditions")
    public boolean duplicateMultipliers = false;

    @Schema(description = "[optional] duplicate pipelines with steps, condition sets and conditions")
    public boolean duplicatePipelines = false;

    @Schema(description = "[optional] duplicate erasers with condition sets and conditions")
    public boolean duplicateErasers = false;

    @Schema(description = "[optional] duplicate triggers with condition sets, conditions")
    public boolean duplicateTriggers = false;

    @Schema(description = "[optional] duplicate condition set with condition sets, conditions")
    public boolean duplicateConditionSets = false;

    @Schema(description = "[optional] Recursively duplicate EVERY factory reachable through branch/pipeline "
            + "nextTwinFactoryId and remap the FK to the clone. Each cascaded factory is duplicated FULLY "
            + "(all its branches, multipliers, pipelines, erasers, triggers, condition sets) and keeps "
            + "cascading through its own next/afterCommit factories — the whole reachable factory graph is "
            + "cloned, not a single factory. Granular flags above apply only to the top-level factory. "
            + "Cycles and self-references are handled (each original is cloned exactly once).")
    public boolean duplicateNextFactoryCascade = false;

    @Schema(description = "[optional] Duplicate the factory reachable through pipeline.afterCommitTwinFactoryId "
            + "and remap the FK to the clone. That factory is duplicated FULLY (all its branches, multipliers, "
            + "pipelines, erasers, triggers, condition sets). Independent from duplicateNextFactoryCascade — "
            + "the two flags are orthogonal.")
    public boolean duplicateAfterCommitFactory = false;
}
