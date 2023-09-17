package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinClassDuplicateRqV1")
public class TwinClassDuplicateRqDTOv1 {
    @Schema(description = "new class key", example = "PROJECT")
    public String newKey;
}
