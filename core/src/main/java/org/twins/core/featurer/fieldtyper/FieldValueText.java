package org.twins.core.featurer.fieldtyper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldValueText extends FieldValue {
    private String value;
}
