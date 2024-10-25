package org.twins.core.dto.rest.validator.cud;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.validator.TransitionValidatorRuleBaseDTOv1;

@Data
@Accessors(chain = true)
@Schema(name = "ValidatorUpdateV1")
public class TransitionValidatorRuleUpdateDTOv1 extends TransitionValidatorRuleBaseDTOv1 {
    //todo think about cud logic
}
