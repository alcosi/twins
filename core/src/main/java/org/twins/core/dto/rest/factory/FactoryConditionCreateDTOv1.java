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
@Schema(name = "FactoryConditionCreateV1")
public class FactoryConditionCreateDTOv1 {

    @NotNull
    @Schema(description = "factory condition set id", example = DTOExamples.FACTORY_CONDITION_SET_ID)
    public UUID factoryConditionSetId;

    @NotNull
    @Schema(description = "conditioner feature id", example = DTOExamples.CONDITIONER_FEATURE_ID)
    public Integer conditionerFeatureId;

    @Schema(description = "conditioner params", example = DTOExamples.CONDITIONER_PARAMS_MAP)
    public HashMap<String, String> conditionerParams;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @Schema(description = "is active", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean active;

    @Schema(description = "is invert", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean invert;

}
