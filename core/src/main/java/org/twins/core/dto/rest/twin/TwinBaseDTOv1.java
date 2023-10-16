package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.time.Instant;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinBaseDTOv1")
public class TwinBaseDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_ID)
    public UUID id;

    @Schema(description = "externalId", example = "934599502DFFAE")
    public String externalId;

    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public Instant createdAt;

    @Schema(description = "name", example = "Oak")
    public String name;

    @Schema(description = "description", example = "The biggest tree")
    public String description;
}
