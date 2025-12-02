package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinValidatorSetBaseV1")
public class TwinValidatorSetBaseDTOv1 {

    @Schema(description = "id")
    public UUID id;

    @Schema(description = "domain id")
    public UUID domainId;

    @Schema(description = "name")
    public String name;

    @Schema(description = "description")
    public String description;

}


