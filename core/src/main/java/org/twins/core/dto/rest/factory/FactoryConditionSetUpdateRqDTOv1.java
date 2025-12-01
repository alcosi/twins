package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryConditionSetUpdateRqV1")
public class FactoryConditionSetUpdateRqDTOv1 extends Request {

    @Schema(description = "condition set list")
    public List<FactoryConditionSetUpdateDTOv1> conditionSets;
}
