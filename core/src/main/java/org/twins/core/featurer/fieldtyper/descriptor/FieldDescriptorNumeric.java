package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldDescriptorNumeric extends FieldDescriptor {
    private Double min;
    private Double max;
    private Double step;
    private Set<String> thousandSeparator;
    private Set<String> decimalSeparator;
    private Integer decimalPlaces;
    private Boolean round;
}
