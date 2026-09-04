package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinClassFieldValidatorUpdateV1")
public class TwinClassFieldValidatorUpdateDTOv1 extends TwinClassFieldValidatorSaveDTOv1 {

    @NotNull
    @Schema(description = "id", example = DTOExamples.UUID_ID)
    public UUID id;
}
