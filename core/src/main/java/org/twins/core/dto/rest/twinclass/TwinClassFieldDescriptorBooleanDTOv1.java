package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.domain.enum_.twin.CheckboxType;

@Data
@Accessors(fluent = true)
@Schema(name = "TwinClassFieldDescriptorBooleanV1")
public class TwinClassFieldDescriptorBooleanDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "booleanV1";
    @Override
    public String fieldType() {
        return KEY;
    }

    @Schema(description = "Checkbox type", example = "TOGGLE")
    public CheckboxType checkboxType;

    @Schema(description = "Nullable flag", example = "false")
    public Boolean nullable;
}
