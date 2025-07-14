package org.twins.core.dto.rest.statistic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "StatisticCalcRqV1")
public class StatisticCalcRqDTOv1 {
    @Schema(description = "Twin id set")
    public Set<UUID> twinIdSet;
}
