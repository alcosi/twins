package org.twins.face.dto.rest.twidget.tw005;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceTW005ButtonV1")
public class FaceTW005ButtonDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "label")
    public String label;

    @Schema(description = "twin class field id")
    public UUID transitionId;

    @Schema(description = "order")
    public Integer order;

    @Schema(description = "Icon url. Might be relative")
    public String icon;

    @Schema(description = "button extra style attributes")
    public Map<String, String> styleAttributes;
}
