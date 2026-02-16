package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TransitionTriggerV1")
public class TransitionTriggerDTOv1 {
    @Schema(description = "id", example = DTOExamples.TRIGGER_ID)
    public UUID id;

    @Schema(description = "domain id", example = DTOExamples.DOMAIN_ID)
    public UUID domainId;

    @Schema(description = "twin trigger featurer id", example = DTOExamples.INTEGER)
    public Integer twinTriggerFeaturerId;

    @Schema(description = "twin trigger params", example = DTOExamples.FACTORY_PARAMS_MAP)
    public HashMap<String, String> twinTriggerParam;

    @Schema(description = "active", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean active;
}
