package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinClassFieldRuleRsV1")
public class TwinClassFieldRuleRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "created rules")
    public List<TwinClassFieldRuleDTOv1> rules;
}
