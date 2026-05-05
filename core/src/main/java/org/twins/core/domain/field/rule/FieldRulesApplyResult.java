package org.twins.core.domain.field.rule;

import lombok.Getter;
import lombok.Setter;
import org.cambium.common.kit.Kit;

import java.util.UUID;

public class FieldRulesApplyResult extends Kit<FieldRuleOutput, UUID> {
    @Getter
    @Setter
    private Boolean allRequiredFieldsFilled = null;
    public static final FieldRulesApplyResult EMPTY = new FieldRulesApplyResult();

    public FieldRulesApplyResult() {
        super(FieldRuleOutput::getTwinClassFieldId);
    }
}
