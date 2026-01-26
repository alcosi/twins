package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.comment.TwinCommentAction;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinCommentActionAlienValidatorRuleBaseV1")
public class TwinCommentActionAlienValidatorRuleBaseDTOv1 extends ValidatorRuleBaseDTOv1 {

    @Schema(description = "twinclass id")
    private UUID twinClassId;

    @Schema(description = "comment action")
    public TwinCommentAction commentAction;

}
