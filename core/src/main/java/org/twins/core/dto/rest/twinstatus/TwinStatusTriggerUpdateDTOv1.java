package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "TwinStatusTriggerUpdateV1")
public class TwinStatusTriggerUpdateDTOv1 extends TwinStatusTriggerSaveDTOv1 {
    @Schema(description = "id")
    public UUID id;
}
