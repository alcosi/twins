package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "ProjectionTypeGroupUpdateV1")
public class ProjectionTypeGroupUpdateDTOv1 {
    @NotNull
    @Schema(description = "id", example = DTOExamples.UUID_ID)
    public UUID id;

    @Schema(description = "key", example = "media")
    public String key;
}
