package org.twins.core.dto.rest.scheduler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.domain.DataTimeRange;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "SchedulerSearchV1")
public class SchedulerSearchDTOv1 {

    @Schema(description = "id set")
    public Set<UUID> idSet;

    @Schema(description = "id exclude set")
    public Set<UUID> idExcludeSet;

    @Schema(description = "id set")
    public Set<Integer> featurerIdSet;

    @Schema(description = "id exclude set")
    public Set<Integer> featurerIdExcludeSet;

    @Schema(description = "active", example = DTOExamples.TERNARY)
    public Ternary active;

    @Schema(description = "logEnabled", example = DTOExamples.TERNARY)
    public Ternary logEnabled;

    @Schema(description = "cron set")
    public Set<String> cronSet;

    @Schema(description = "cron exclude set")
    public Set<String> cronExcludeSet;

    @Schema(description = "fixed rate set")
    public Set<Integer> fixedRateSet;

    @Schema(description = "fixed rate exclude set")
    public Set<Integer> fixedRateExcludeSet;

    @Schema(description = "description set")
    public Set<String> descriptionSet;

    @Schema(description = "description exclude set")
    public Set<String> descriptionExcludeSet;

    @Schema(description = "created at")
    public DataTimeRange createdAt;

    @Schema(description = "updated at")
    public DataTimeRange updatedAt;
}
