package org.twins.core.dto.rest.statistic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "StatisticCalcRsV1")
public class StatisticCalcRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "Statistic map")
    public TwinStatisticProgressPercentDTOv1 statistics;
}
