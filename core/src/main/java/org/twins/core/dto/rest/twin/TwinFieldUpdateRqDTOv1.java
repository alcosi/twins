package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinFieldUpdateRqV1")
public class TwinFieldUpdateRqDTOv1 extends Request {
    @Schema(description = "On of values", example = "")
    public TwinFieldValueDTO value;
}
