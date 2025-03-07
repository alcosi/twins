package org.twins.core.dto.rest.twinstatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinStatusV1")
public class TwinStatusIdsExtenderDTOv1 {
    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;

    @Schema(description = "add extensible Twin class identifiers", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean addExtendableTwinClassIds;
}
