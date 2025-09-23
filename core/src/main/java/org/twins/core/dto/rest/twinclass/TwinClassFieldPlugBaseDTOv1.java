package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinClassFieldPlugBaseV1")
public class TwinClassFieldPlugBaseDTOv1 {

    @Schema(example = DTOExamples.TWIN_CLASS_ID)
    private UUID twinClassId;

    @Schema(example = DTOExamples.TWIN_CLASS_FIELD_ID)
    private UUID twinClassFieldId;
}
