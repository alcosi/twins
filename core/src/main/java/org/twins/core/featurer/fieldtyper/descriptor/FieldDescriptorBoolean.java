package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinFieldBooleanEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldDescriptorBoolean extends FieldDescriptor {

    private TwinFieldBooleanEntity.CheckboxType checkboxType;
    private Boolean nullable;
}
