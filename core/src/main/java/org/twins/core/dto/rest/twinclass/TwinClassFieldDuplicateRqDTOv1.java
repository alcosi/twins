package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinClassFieldDuplicateRqV1")
public class TwinClassFieldDuplicateRqDTOv1 {

    @Schema(description = "twin class field id", example = DTOExamples.UUID_ID)
    public UUID twinClassFieldId;

    @Schema(description = "new field key", example = DTOExamples.TWIN_FIELD_KEY)
    public String newKey;
}
