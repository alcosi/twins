package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryRsV1")
public class FactoryRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - factory")
    public FactoryDTOv2 factory;
}
