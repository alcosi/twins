package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinStatusDTOv1")
public class TwinStatusDTOv1 {
    @Schema(description = "id", example = "c2a7f81f-d7da-43e8-a1d3-18d6f632878b")
    public UUID id;

    @Schema(description = "name", example = "PLN")
    public String name;

    @Schema(description = "description", example = "PLN")
    public String description;

    @Schema(description = "logo", example = "PLN")
    public String logo;
}
