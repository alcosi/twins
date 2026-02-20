package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "TwinFactoryTriggerUpdateV1")
public class TwinFactoryTriggerUpdateDTOv1 extends TwinFactoryTriggerSaveDTOv1 {
    @Schema(description = "id")
    public UUID id;
}
