package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinClassMapV1")
public class TwinClassMapDTOv1 {
    @Schema(description = "twin classs id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;

    @Schema(description = "include parent fields", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean includeParentFields;
}
