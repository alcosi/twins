package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryConditionSetCreateV1")
public class FactoryConditionSetCreateDTOv1 {

    @NotNull
    @Schema(description = "twin factory id")
    public UUID twinFactoryId;

    @Schema(description = "name", example = DTOExamples.NAME)
    public String name;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @Schema(description = "cachable", example = "false")
    public Boolean cachable;

}
