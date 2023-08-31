package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinStatusDTOv1")
public class TwinStatusDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_STATUS_ID)
    public UUID id;

    @Schema(description = "name", example = "PLN")
    public String name;

    @Schema(description = "description", example = "PLN")
    public String description;

    @Schema(description = "logo", example = "PLN")
    public String logo;
}
