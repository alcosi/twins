package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryMultiplierSaveV1")
public class FactoryMultiplierSaveDTOv1 {
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


