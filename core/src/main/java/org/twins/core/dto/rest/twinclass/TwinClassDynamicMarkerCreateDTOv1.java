package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinClassDynamicMarkerCreateV1")
public class TwinClassDynamicMarkerCreateDTOv1 {

    @NotNull
    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;

    @NotNull
    @Schema(description = "twin validator set id")
    public UUID twinValidatorSetId;

    @NotNull
    @Schema(description = "marker data list option id")
    public UUID markerDataListOptionId;
}
