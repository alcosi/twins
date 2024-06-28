package org.twins.core.dto.rest.twinclass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.common.BasicUpdateOperationDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinClassUpdateRqV1")
public class TwinClassUpdateRqDTOv1 extends TwinClassSaveRqDTOv1 {
    @Schema(description = "[optional] should be filled on change marker data list id")
    public BasicUpdateOperationDTOv1 markerDataListUpdate;

    @Schema(description = "[optional] should be filled on change tag data list id")
    public BasicUpdateOperationDTOv1 tagDataListUpdate;

    @Schema(description = "[optional] should be filled on change extends twins class id")
    public BasicUpdateOperationDTOv1 extendsTwinClassUpdate;

    @Schema(description = "[optional] should be filled on change extends twins class id")
    public BasicUpdateOperationDTOv1 headTwinClassUpdate;

    @JsonIgnore
    public UUID twinClassId;
}
