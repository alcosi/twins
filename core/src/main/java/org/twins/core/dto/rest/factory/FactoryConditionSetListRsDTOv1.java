package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FactoryConditionSetListRsV1")
public class FactoryConditionSetListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - condition list")
    public List<FactoryConditionSetDTOv1> conditionSets;
}
