package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinClassFieldDuplicateV1")
public class TwinClassFieldDuplicateDTOv1 {
    @Schema(description = "original twin class field id")
    public UUID originalTwinClassFieldId;

    @Schema(description = "[optional] fill if field should be copied to other class")
    public UUID newTwinClassId;

    @Schema(description = "new field key", example = DTOExamples.TWIN_FIELD_KEY)
    public String newKey;

    @Schema(description = "[optional] duplicate all rules")
    public boolean duplicateRules = false;
}
