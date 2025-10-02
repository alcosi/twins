package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldConditionElementType;

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

    @Schema(description = "parameter key to look at when targetElement = param")
    public String targetParamKey;

    @Schema(description = "value that will be written to the dependent field (or its parameter) when the rule fires")
    public String dependentOverwrittenValue;

    @Schema(description = "datalist id that will be written to the dependent field when the rule fires")
    public UUID dependentOverwrittenDatalistId;

    @Schema(description = "priority – lower value means the rule will be evaluated earlier")
    public Integer rulePriority;

    @Schema(description = "list of atomic conditions that make up the rule")
    public List<TwinClassFieldConditionCreateDTOv1> conditions;

}
