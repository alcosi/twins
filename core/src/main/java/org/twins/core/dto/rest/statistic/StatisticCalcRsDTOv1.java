package org.twins.core.dto.rest.statistic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.domain.TwinStatistic;
import org.twins.core.dto.rest.Response;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "StatisticCalcRsV1")
public class StatisticCalcRsDTOv1 extends Response {
    @Schema(description = "Statistic map")
    public Map<UUID, TwinStatistic> statistics;
}
