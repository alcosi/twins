package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldDescriptorNumeric extends FieldDescriptor {
    private Double min;
    private Double max;
    private Double step;
    private String thousandSeparator;
    private String decimalSeparator;
    private Integer decimalPlaces;
}
