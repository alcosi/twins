package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinValidatorSetV1")
public class TwinValidatorSetDTOv1 {

    @Schema(description = "id")
    public UUID id;

    @Schema(description = "name")
    public String name;

    @Schema(description = "description")
    public String description;

    @Schema(description = "invert")
    public Boolean invert;

    @Schema(description = "Number of times this validator set is referenced from configuration tables (read-only, maintained by DB triggers)")
    public Integer usageCount;

}
