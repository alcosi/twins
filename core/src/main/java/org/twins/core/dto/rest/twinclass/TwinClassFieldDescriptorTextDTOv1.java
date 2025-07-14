package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.featurer.fieldtyper.FieldTyperTextField;

@Data
@Accessors(fluent = true)
@Schema(name =  TwinClassFieldDescriptorTextDTOv1.KEY)
public class TwinClassFieldDescriptorTextDTOv1 implements TwinClassFieldDescriptorDTO {

    public static final String KEY = "TwinClassFieldDescriptorTextV1";

    public TwinClassFieldDescriptorTextDTOv1() {
        this.fieldType = KEY;
    }

    @Schema(description = "Field type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String fieldType;

    @Schema(description = "Some validation regexp", example = ".*")
    public String regExp;

    @Schema(description = "Type of editor", example = "PLAIN")
    public FieldTyperTextField.TextEditorType editorType;
}
