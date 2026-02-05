package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassDynamicMarkerUpdateV1")
public class TwinClassDynamicMarkerUpdateDTOv1 extends TwinClassDynamicMarkerSaveDTOv1 {

    @Schema(description = "id")
    public UUID id;

    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;

    @Schema(description = "twin validator set id")
    public UUID twinValidatorSetId;

    @Schema(description = "marker data list option id")
    public UUID markerDataListOptionId;
}
