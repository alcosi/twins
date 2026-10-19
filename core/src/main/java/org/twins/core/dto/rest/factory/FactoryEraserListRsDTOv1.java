package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryEraserListRsV1")
public class FactoryEraserListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - factory eraser list")
    public List<FactoryEraserDTOv1> factoryEraserList;
}
