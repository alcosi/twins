package org.twins.core.dto.rest.scheduler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;
import org.twins.core.dto.rest.IntegerRangeDTOv1;

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

    @Schema(description = "domain id set")
    public Set<UUID> domainIdSet;

    @Schema(description = "domain id exclude set")
    public Set<UUID> domainIdExcludeSet;

    @Schema(description = "id set")
    public Set<Integer> featurerIdSet;

    @Schema(description = "id exclude set")
    public Set<Integer> featurerIdExcludeSet;

    @Schema(description = "active", example = DTOExamples.TERNARY)
    public Ternary active;

    @Schema(description = "logEnabled", example = DTOExamples.TERNARY)
    public Ternary logEnabled;

    @Schema(description = "cron like set")
    public Set<String> cronLikeSet;

    @Schema(description = "cron not like set")
    public Set<String> cronNotLikeSet;

    @Schema(description = "fixed rate range")
    public IntegerRangeDTOv1 fixedRateRange;

    @Schema(description = "description like set")
    public Set<String> descriptionLikeSet;

    @Schema(description = "description not like set")
    public Set<String> descriptionNotLikeSet;

    @Schema(description = "created at range")
    public DataTimeRangeDTOv1 createdAtRange;

    @Schema(description = "updated at range")
    public DataTimeRangeDTOv1 updatedAtRange;
}
