package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldV1")
public class TwinClassFieldDTOv1 {
    @Schema(description = "id", example = "c2a7f81f-d7da-43e8-a1d3-18d6f632878b")
    public UUID id;

    @Schema(description = "key", example = "PROJECT")
    public String key;

    @Schema(description = "name", example = "Oak")
    public String name;

    @Schema(description = "description", example = "The biggest tree")
    public String description;
}
