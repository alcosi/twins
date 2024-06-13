package org.twins.core.dto.rest.featurer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

@Data
@Accessors(chain = true)
@Schema(name = "FeaturerParamV1")
public class FeaturerParamDTOv1 {
    @Schema(description = "key", example = DTOExamples.FEATURER_PARAM_NAME)
    public String key;

    @Schema(description = "name", example = DTOExamples.FEATURER_PARAM_NAME)
    public String name;

    @Schema(description = "description")
    public String description;

    @Schema(description = "type")
    public String type;
}
