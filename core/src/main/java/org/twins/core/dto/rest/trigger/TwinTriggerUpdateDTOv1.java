package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "TwinTriggerUpdateV1")
public class TwinTriggerUpdateDTOv1 extends TwinTriggerDTOv1 {
    @Schema(description = "id", example = DTOExamples.UUID_ID)
    public UUID id;
}
