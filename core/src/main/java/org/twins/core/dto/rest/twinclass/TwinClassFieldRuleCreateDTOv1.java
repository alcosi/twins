package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldConditionElementType;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinClassFieldRuleCreateV1")
public class TwinClassFieldRuleCreateDTOv1 {

    @Schema(description = "field whose value or parameter will be overwritten if the rule fires")
    public UUID dependentTwinClassFieldId;

    @Schema(description = "what part of the base field we should look at: value | param")
    public TwinClassFieldConditionElementType targetElement;

    @Schema(description = "value that will be written to the dependent field (or its parameter) when the rule fires")
    public String dependentOverwrittenValue;

    @Schema(description = "parameter key to look at when targetElement = param")
    public String targetParamKey;

    @Schema(description = "whether the dependent field required param must be set (true) or unset (false) for the rule to fire")
    public Boolean required;

    @Schema(description = "Field overwriter featurer ID", example = "1")
    public Integer fieldOverwriterFeaturerId;

    @Schema(description = "Field overwriter parameters", example = "{}")
    public HashMap<String, String> fieldOverwriterParams;

    @Schema(description = "priority – lower value means the rule will be evaluated earlier")
    public Integer rulePriority;

    @Schema(description = "list of atomic conditions that make up the rule")
    public List<TwinClassFieldConditionCreateDTOv1> conditions;

}
