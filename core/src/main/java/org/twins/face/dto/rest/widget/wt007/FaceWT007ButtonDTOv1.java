package org.twins.face.dto.rest.widget.wt007;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceWT007ButtonV1")
public class FaceWT007ButtonDTOv1 {
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

    @Schema(description = "styles, converted to css classes")
    public Set<String> styleClasses;

    @Schema(description = "is showing needed when button inactive")
    public Boolean showWhenInactive;
}
