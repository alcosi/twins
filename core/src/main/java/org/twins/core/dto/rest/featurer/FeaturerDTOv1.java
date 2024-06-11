package org.twins.core.dto.rest.featurer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "FeaturerV1")
public class FeaturerDTOv1 {
    @Schema(description = "id", example = DTOExamples.FEATURER_ID)
    public int id;

    @Schema(description = "featurer type id", example = "12")
    public int featurerTypeId;

    @Schema(description = "name", example = DTOExamples.FEATURER_NAME)
    public String name;

    @Schema(description = "description")
    public String description;

    @Schema(description = "params list")
    public List<FeaturerParamDTOv1> params;
}
