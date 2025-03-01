package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryConditionSetViewRsV1")
public class FactoryConditionSetViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "result - condition set")
    public FactoryConditionSetDTOv1 conditionSet;
}
