package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "ProjectionTypeGroupSaveV1")
public class ProjectionTypeGroupSaveDTOv1 {
    @NotBlank
    @Schema(description = "key", example = "media")
    public String key;
}
