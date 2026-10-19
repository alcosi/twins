package org.twins.core.dto.rest.usage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.enums.usage.UsageType;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "UsageV1")
public class UsageDTOv1 {
    @Schema(description = "usage type (where the object is used)", example = "TWINFLOW_TRANSITION_INBUILT_FACTORY")
    public UsageType usageType;

    @Schema(description = "id of the entity that uses the object", example = DTOExamples.TWINFLOW_TRANSITION_ID)
    public UUID id;
}
