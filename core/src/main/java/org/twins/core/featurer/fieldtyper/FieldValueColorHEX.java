package org.twins.core.featurer.fieldtyper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldValueColorHEX extends FieldValue {
    private String hex;
}
