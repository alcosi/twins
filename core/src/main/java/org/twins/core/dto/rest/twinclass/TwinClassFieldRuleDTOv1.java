package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;

import java.util.List;
import java.util.UUID;

/**
 * REST representation of {@link TwinClassFieldRuleEntity}.
 * <p>
 * It contains all rule-level settings as well as the list of atomic
 * {@link TwinClassFieldConditionDTOv1 conditions} that must be evaluated in order to decide
 * whether the rule fires.
 */
@Data
@Accessors(chain = true)
@Schema(name = "TwinClassFieldRuleV1")
public class TwinClassFieldRuleDTOv1 {

    @Schema(description = "id")
    public UUID id;

    @Schema(description = "field whose value or parameter will be overwritten if the rule fires")
    public UUID twinClassFieldId;

    @Schema(description = "value that will be written to the dependent field (or its parameter) when the rule fires")
    public String overwrittenValue;

    @Schema(description = "whether the dependent field required param must be set (true) or unset (false) for the rule to fire")
    public Boolean overwrittenRequired;

    @Schema(description = "priority – lower value means the rule will be evaluated earlier")
    public Integer rulePriority;

    @Schema(description = "rule overwritten field descriptor", example = "")
    public TwinClassFieldDescriptorDTO overwrittenDescriptor;

    @Schema(description = "list of atomic conditions that make up the rule")
    public List<TwinClassFieldConditionDTOv1> conditions;

}
