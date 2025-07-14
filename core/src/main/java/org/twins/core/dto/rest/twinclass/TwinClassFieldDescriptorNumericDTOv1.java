package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorNumericV1")
public class TwinClassFieldDescriptorNumericDTOv1 implements TwinClassFieldDescriptorDTO {

    public static final String KEY = "numericFieldV1";

    public TwinClassFieldDescriptorNumericDTOv1() {
        this.fieldType = KEY;
    }

    @Schema(description = "Field type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String fieldType;

    @Schema(description = "Min possible value", example = "1")
    public Double min;

    @Schema(description = "Max possible value", example = "10")
    public Double max;

    @Schema(description = "Step of value change", example = "1")
    public Double step;

    @Schema(description = "Thousand separator. Must not be equal to decimal separator.", example = ",")
    public String thousandSeparator;

    @Schema(description = "Decimal separator. Must not be equal to thousand separator.", example = ".")
    public String decimalSeparator;

    @Schema(description = "Number of decimal places.", example = "0")
    public Integer decimalPlaces;


}
