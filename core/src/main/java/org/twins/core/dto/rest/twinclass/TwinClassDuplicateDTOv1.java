package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinClassDuplicateV1")
public class TwinClassDuplicateDTOv1 {
    @Schema(description = "original twin class id")
    public UUID originalTwinClassId;

    @Schema(description = "new class key", example = "PROJECT")
    public String newKey;

    @Schema(description = "[optional] duplicate all class fields")
    public boolean duplicateFields = false;

    @Schema(description = "[optional] duplicate all class statuses")
    public boolean duplicateStatuses = false;
}
