package org.twins.core.dto.rest.statistic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinStatisticProgressPercentV1")
public class TwinStatisticProgressPercentDTOv1 {
    @Schema(description = "Item list")
    public List<TwinStatisticProgressPercentItemDTOv1> items;
}
