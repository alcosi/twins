package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.domain.enum_.featurer.fieldtyper.TextEditorType;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldDescriptorText extends FieldDescriptor {
    private String regExp;
    private TextEditorType editorType;
    private boolean unique;
}
