package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinClassDynamicMarkerV1")
public class TwinClassDynamicMarkerDTOv1 {

    @Schema(description = "id")
    public UUID id;

    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "twinClass")
    public UUID twinClassId;

    @Schema(description = "twin validator set id")
    public UUID twinValidatorSetId;

    @Schema(description = "marker data list option id")
    @RelatedObject(type = DataListOptionDTOv1.class, name = "markerDataListOption")
    public UUID markerDataListOptionId;
}
