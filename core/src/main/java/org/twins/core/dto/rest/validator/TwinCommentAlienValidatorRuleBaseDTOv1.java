package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.comment.TwinCommentAction;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinCommentAlienValidatorRuleBaseV1")
public class TwinCommentAlienValidatorRuleBaseDTOv1 extends ValidatorRuleBaseDTOv1 {

    @Schema(description = "twinclass id")
    private UUID twinClassId;

    @Schema(description = "comment action")
    public TwinCommentAction commentAction;

}
