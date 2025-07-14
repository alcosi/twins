package org.twins.core.dto.rest.statistic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "ItemV1")
public class ItemDTOv1 {
    @Schema(description = "label", example = "In progress")
    public String label;
    @Schema(description = "key", example = "inProgress")
    public String key;
    @Schema(description = "percent", example = "30")
    public int percent;
    @Schema(description = "color", example = "#35dd68")
    public String colorHex;
}
