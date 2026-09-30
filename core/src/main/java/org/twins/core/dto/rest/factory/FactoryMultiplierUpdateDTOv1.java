package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryMultiplierUpdateV1")
public class FactoryMultiplierUpdateDTOv1 {

    @NotNull
    @Schema(description = "factory multiplier id", example = DTOExamples.FACTORY_MULTIPLIER_ID)
    public UUID id;

    @Schema(description = "input twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID inputTwinClassId;

    @Schema(description = "multiplier featurer id", example = DTOExamples.FEATURER_ID)
    public Integer multiplierFeaturerId;

    @Schema(description = "multiplier params", example = DTOExamples.FACTORY_PARAMS_MAP)
    public HashMap<String, String> multiplierParams;

    @Schema(description = "is active", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean active;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;
}
