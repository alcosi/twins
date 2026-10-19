package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassFieldRuleListRsV1")
public class TwinClassFieldRuleListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "twin class field rule list")
    public List<TwinClassFieldRuleDTOv1> fieldRules;
}
