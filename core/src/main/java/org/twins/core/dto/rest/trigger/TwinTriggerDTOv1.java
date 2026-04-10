package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinTriggerV1")
public class TwinTriggerDTOv1 {
    @Schema(description = "id", example = DTOExamples.TRIGGER_ID)
    public UUID id;

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

    @Schema(description = "job twin class id")
    @RelatedObject(type = TwinClassDTOv1.class, name = "jobTwinClass")
    public UUID jobTwinClassId;
}
