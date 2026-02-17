package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.Map;

@Data
@Accessors(chain = true)
@Schema(name = "TwinTriggerSaveV1")
public class TwinTriggerSaveDTOv1 {
    @Schema(description = "trigger featurer id", example = DTOExamples.FEATURER_ID)
    @RelatedObject(type = FeaturerDTOv1.class, name = "triggerFeaturer")
    private Integer triggerFeaturerId;

    @Schema(description = "trigger params", example = DTOExamples.FACTORY_PARAMS_MAP)
    public Map<String, String> triggerParams;

    @Schema(description = "name", example = DTOExamples.NAME)
    public String name;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @Schema(description = "is active", example = DTOExamples.COUNT)
    public Boolean active;
}
