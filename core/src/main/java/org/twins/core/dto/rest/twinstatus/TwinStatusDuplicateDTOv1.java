package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinStatusDuplicateV1")
public class TwinStatusDuplicateDTOv1 {
    @Schema(description = "original twin status id")
    public UUID originalTwinStatusId;

    @Schema(description = "[optional] fill if status should be copied to other class")
    public UUID newTwinClassId;

    @Schema(description = "new status key", example = "toDo")
    public String newKey;

    @Schema(description = "[optional] duplicate all status triggers")
    public boolean duplicateTriggers = false;
}
