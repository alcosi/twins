package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinClassFieldConditionUpdateV1")
public class TwinClassFieldConditionUpdateDTOv1 extends TwinClassFieldConditionSaveDTOv1 {

    @Schema(description = "id")
    public UUID id;
}
