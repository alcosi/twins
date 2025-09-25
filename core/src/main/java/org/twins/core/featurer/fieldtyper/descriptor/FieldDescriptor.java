package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public abstract class FieldDescriptor {
    private boolean backendValidated;
}
