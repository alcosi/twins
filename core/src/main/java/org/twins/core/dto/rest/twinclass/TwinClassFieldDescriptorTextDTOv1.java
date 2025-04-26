package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.featurer.fieldtyper.FieldTyperTextField;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorTextV1")
public class TwinClassFieldDescriptorTextDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "textV1";
    public String fieldType = KEY;

    @Schema(description = "Some validation regexp", example = ".*")
    public String regExp;

    @Schema(description = "Type of editor", example = "PLAIN")
    public FieldTyperTextField.TextEditorType editorType;
}
