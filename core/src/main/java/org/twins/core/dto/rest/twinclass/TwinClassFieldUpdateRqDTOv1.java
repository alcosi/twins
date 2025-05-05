package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassFieldUpdateRqV1")
public class TwinClassFieldUpdateRqDTOv1 extends Request {
    @Schema(description = "twin class field update")
    public TwinClassFieldUpdateDTOv1 twinClassFieldUpdate;
}
