package org.twins.core.dto.rest.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.widget.WidgetDTOv1;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "CardWidgetV1")
public class CardWidgetDTOv1 {
    @Schema(description = "id", example = "c2a7f81f-d7da-43e8-a1d3-18d6f632878b")
    public UUID id;

    @Schema(description = "layoutPosition", example = "FirstColumn")
    public String layoutPositionKey;

    @Schema(description = "in layout position order", example = "1")
    public int inPositionOrder;

    @Schema(description = "name", example = "Details")
    public String name;

    @Schema(description = "color", example = "red")
    public String color;

    @Schema(description = "widget", example = "")
    public WidgetDTOv1 widget;

}
