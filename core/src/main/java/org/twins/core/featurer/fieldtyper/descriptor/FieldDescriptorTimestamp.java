package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldDescriptorTimestamp extends FieldDescriptor {
    private String pattern;
    private LocalDateTime beforeDate;
    private LocalDateTime afterDate;
}
