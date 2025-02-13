package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TransitionAliasV1")
public class TransitionAliasDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWINFLOW_TRANSITION_ALIAS_ID)
    public UUID id;

    @Schema(description = "alias", example = DTOExamples.TWINFLOW_TRANSITION_ALIAS)
    public String alias;

    @Schema(description = "usages count", example = DTOExamples.COUNT)
    public Integer usagesCount;

}
