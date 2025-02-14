package org.twins.core.dto.rest.featurer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.List;

import static org.twins.core.dto.rest.DTOExamples.FEATURER_PARAM_VALUE_INT;
import static org.twins.core.dto.rest.DTOExamples.FEATURER_PARAM_VALUE_LIST;

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

    @Schema(description = "optional", example = "true")
    public Boolean optional;

    @Schema(description = "defaultValue", examples = {FEATURER_PARAM_VALUE_LIST, FEATURER_PARAM_VALUE_INT})
    public String defaultValue;

    @Schema(description = "exampleValues", example = "[\"" + FEATURER_PARAM_VALUE_LIST + "\"," + FEATURER_PARAM_VALUE_INT + "]")
    public List<String> exampleValues;

    @Schema(description = "order")
    public Integer order;
}
