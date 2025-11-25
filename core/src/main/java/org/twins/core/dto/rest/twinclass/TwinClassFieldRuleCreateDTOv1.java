package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinClassFieldRuleCreateV1")
public class TwinClassFieldRuleCreateDTOv1 {

    @Schema(description = "fields whose value or parameter will be overwritten if the rule fires")
    public Set<UUID> dependentTwinClassFieldIds;

    @Schema(description = "value that will be written to the dependent field (or its parameter) when the rule fires")
    public String overwrittenValue;

    @Schema(description = "whether the dependent field required param must be set (true) or unset (false) for the rule to fire")
    public Boolean overwrittenRequired;

    @Schema(description = "Field overwriter featurer ID", example = "1")
    public Integer fieldParamOverwriterFeaturerId;

    @Schema(description = "Field overwriter parameters", example = "{}")
    public HashMap<String, String> fieldParamOverwriterParams;

    @Schema(description = "priority – lower value means the rule will be evaluated earlier")
    public Integer rulePriority;

    @Schema(description = "list of atomic conditions that make up the rule")
    public List<TwinClassFieldConditionTreeCreateDTOv1> conditions;

}
