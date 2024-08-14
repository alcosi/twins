package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TriggerV1")
public class TriggerDTOv1 extends TriggerBaseDTOv1 {
    @Schema(description = "id")
    public UUID id;
}
