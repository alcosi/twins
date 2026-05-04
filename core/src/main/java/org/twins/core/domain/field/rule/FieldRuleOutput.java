package org.twins.core.domain.field.rule;

import lombok.Builder;
import lombok.Data;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class FieldRuleOutput {
    private TwinClassFieldEntity field;
    private Object value;
    private Boolean required;
    private Map<String, String> descriptor;
    private Boolean hasError;

    public UUID getTwinClassFieldId() {
        return field.getId();
    }
}
