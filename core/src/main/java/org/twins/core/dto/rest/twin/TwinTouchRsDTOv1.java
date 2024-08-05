package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinTouchRsV1")
public class TwinTouchRsDTOv1 extends Response {
    @Schema(description = "twin touch")
    public TwinTouchDTOv1 twinTouch;
}
