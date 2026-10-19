package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinValidatorSetUpdateV1")
public class TwinValidatorSetUpdateDTOv1 {

    @NotNull
    @Schema(description = "id", example = DTOExamples.UUID_ID)
    public UUID id;

    @Schema(description = "name")
    public String name;

    @Schema(description = "description")
    public String description;

    @Schema(description = "invert")
    public Boolean invert;

}
