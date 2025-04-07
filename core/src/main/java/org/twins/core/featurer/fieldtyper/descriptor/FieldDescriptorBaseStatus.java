package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinStatusEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldDescriptorBaseStatus extends FieldDescriptor {
    private TwinStatusEntity status;
}
