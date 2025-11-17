package org.twins.core.dto.rest.statistic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "StatisticCalcRsV1")
public class StatisticCalcRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "twin statistic list")
    public Map<UUID, TwinStatisticProgressPercentDTOv1> statistics;
}
