package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryEraserCreateRqV1")
public class FactoryEraserCreateRqDTOv1 extends Request {
    @Schema(description = "factory eraser create")
    public FactoryEraserCreateDTOv1 eraser;
}
