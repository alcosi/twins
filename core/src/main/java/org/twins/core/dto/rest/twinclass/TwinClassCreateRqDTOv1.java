package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinClassCreateRqV1")
public class TwinClassCreateRqDTOv1 extends TwinClassSaveRqDTOv1 {
    @Schema(description = "[optional] link to extends class. All fields and links will be valid for current class. Use ffffffff-ffff-ffff-ffff-ffffffffffff for nullify value", example = "")
    public UUID extendsTwinClassId;

    @Schema(description = "[optional] link to head (parent) class. It should be used in case, when twins of some class can not exist without some parent twin. " +
            "Example: Task and Sub-task. Use ffffffff-ffff-ffff-ffff-ffffffffffff for nullify value", example = "")
    public UUID headTwinClassId;


    @Schema(description = "[optional] id of linked marker list. Markers in some cases similar to secondary statuses. Use ffffffff-ffff-ffff-ffff-ffffffffffff for nullify value", example = "")
    public UUID markerDataListId;

    @Schema(description = "[optional] id of linked tags cloud. Tags differ from markers in that new tags can be added to the cloud by the users themselves. " +
            "And the list of markers is configured only by the domain manager. Use ffffffff-ffff-ffff-ffff-ffffffffffff for nullify value", example = "")
    public UUID tagDataListId;

}
