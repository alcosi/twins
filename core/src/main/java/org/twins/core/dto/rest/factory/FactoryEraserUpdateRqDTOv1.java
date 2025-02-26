package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryEraserUpdateRqV1")
public class FactoryEraserUpdateRqDTOv1 extends Request {
    @Schema(description = "factory eraser update")
    public FactoryEraserUpdateDTOv1 eraser;
}
