package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;

@Data
@Accessors(chain = true)
@Schema(name = "TriggerBaseV1")
public class TriggerBaseDTOv1 {

    @Schema(description = "order")
    public Integer order;

    @Schema(description = "trigger featurer id")
    public Integer triggerFeaturerId;

    @Schema(description = "featurer params")
    public HashMap<String, String> triggerParams;

    @Schema(description = "active")
    public Boolean active;
}
