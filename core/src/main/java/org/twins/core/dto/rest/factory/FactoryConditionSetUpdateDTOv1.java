package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FactoryConditionSetUpdateV1")
public class FactoryConditionSetUpdateDTOv1 extends FactoryConditionSetSaveDTOv1 {

    @Schema(description = "conditionSetId", example = DTOExamples.FACTORY_CONDITION_SET_ID)
    public UUID conditionSetId;
}
