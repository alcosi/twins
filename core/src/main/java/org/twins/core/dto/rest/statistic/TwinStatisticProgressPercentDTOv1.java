package org.twins.core.dto.rest.statistic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.domain.statistic.TwinStatisticProgressPercent;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinStatisticProgressPercentV1")
public class TwinStatisticProgressPercentDTOv1 {
    @Schema(description = "twin statistic list")
    public Map<UUID, TwinStatisticProgressPercent> twinStatistics;
}
