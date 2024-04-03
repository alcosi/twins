package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldDescriptorNumeric extends FieldDescriptor {
    private double min;
    private double max;
    private double step;
    private String thousandSeparator;
    private String decimalSeparator;
    private int decimalPlaces;
}
