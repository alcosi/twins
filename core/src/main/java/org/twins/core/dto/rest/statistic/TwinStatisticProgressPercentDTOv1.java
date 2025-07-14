package org.twins.core.dto.rest.statistic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.domain.statistic.TwinStatisticProgressPercent;
import org.twins.face.dto.rest.navbar.nb001.FaceNB001MenuItemDTOv1;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinStatisticProgressPercentV1")
public class TwinStatisticProgressPercentDTOv1 {
    @Schema(description = "Item list")
    public List<ItemDTOv1> items;
}
