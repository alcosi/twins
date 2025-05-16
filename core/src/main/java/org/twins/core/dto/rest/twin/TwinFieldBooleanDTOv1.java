package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

@Data
@Accessors(chain = true)
@Schema(name = "TwinFieldBooleanV1")
public class TwinFieldBooleanDTOv1 {
    @Schema(description = "boolean value", example = DTOExamples.BOOLEAN_TRUE)
    public boolean value;
}
