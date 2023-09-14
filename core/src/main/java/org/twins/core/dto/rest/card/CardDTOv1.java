package org.twins.core.dto.rest.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "CardV1")
public class CardDTOv1 {
    @Schema(description = "id", example = "c2a7f81f-d7da-43e8-a1d3-18d6f632878b")
    public UUID id;

    @Schema(description = "key", example = "details")
    public String key;

    @Schema(description = "name", example = "Details")
    public String name;

    @Schema(description = "logo", example = "http://twins.org/t/card/main.png")
    public String logo;

    @Schema(description = "layout", example = "OneColumn")
    public String layoutKey;

    @Schema(description = "Class fields list")
    public List<CardWidgetDTOv1> widgets;
}
