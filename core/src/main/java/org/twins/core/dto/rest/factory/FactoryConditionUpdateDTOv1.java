package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryConditionUpdateV1")
public class FactoryConditionUpdateDTOv1 extends FactoryConditionSaveDTOv1{

    @Schema(description = "factory condition id", example = DTOExamples.FACTORY_CONDITION_ID)
    public UUID id;
}
