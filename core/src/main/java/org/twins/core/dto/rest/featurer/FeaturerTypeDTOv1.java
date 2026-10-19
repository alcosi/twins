package org.twins.core.dto.rest.featurer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "FeaturerTypeV1")
public class FeaturerTypeDTOv1 {
    @Schema(description = "id", example = "12")
    public int id;

    @Schema(description = "name")
    public String name;

    @Schema(description = "description")
    public String description;
}
