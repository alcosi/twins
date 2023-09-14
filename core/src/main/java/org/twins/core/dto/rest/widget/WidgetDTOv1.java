package org.twins.core.dto.rest.widget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "WidgetV1")
public class WidgetDTOv1 {
    @Schema(description = "id", example = DTOExamples.WIDGET_ID)
    public UUID id;

    @Schema(description = "key", example = "details")
    public String key;

    @Schema(description = "name", example = "Details")
    public String name;
}
