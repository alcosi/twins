package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldTypeParamV1")
public class TwinClassFieldTypeParamDTOv1 {
    @Schema(description = "key", example = "regexp")
    public String key;

    @Schema(description = "value", example = ".*")
    public String value;
}
