package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.action.TwinAction;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinActionValidatorRuleBaseV1")
public class TwinActionValidatorRuleBaseDTOv1 extends ValidatorRuleBaseDTOv1 {

    @Schema(description = "twinclass id")
    private UUID twinClassId;

    @Schema(description = "twin action")
    public TwinAction twinAction;

}
