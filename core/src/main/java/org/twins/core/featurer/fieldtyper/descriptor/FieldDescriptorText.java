package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.featurer.fieldtyper.FieldTyperTextField;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldDescriptorText extends FieldDescriptor {
    private String regExp;
    private FieldTyperTextField.TextEditorType editorType;
}
