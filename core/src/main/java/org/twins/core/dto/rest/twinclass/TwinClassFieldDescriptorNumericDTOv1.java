package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorNumericV1")
public class TwinClassFieldDescriptorNumericDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "numericFieldDescriptorV1";
    public String fieldType = KEY;

    @Schema(description = "Min possible value", example = "1")
    public Integer min;

    @Schema(description = "Max possible value", example = "10")
    public Integer max;

    @Schema(description = "Step of value change", example = "1")
    public Integer step;

    @Schema(description = "Thousand separator", example = ",")
    public String thousandSeparator;

}
