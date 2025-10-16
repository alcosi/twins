package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorNumericV1")
public class TwinClassFieldDescriptorNumericDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "numericFieldV1";
    @Override
    public String fieldType() {
        return KEY;
    }

    @Schema(description = "Min possible value", example = "1")
    public Double min;

    @Schema(description = "Max possible value", example = "10")
    public Double max;

    @Schema(description = "Step of value change", example = "1")
    public Double step;

    @Schema(description = "Thousand separator. Must not be equal to decimal separator.", example = ",")
    public Set<String> thousandSeparator;

    @Schema(description = "Decimal separator. Must not be equal to thousand separator.", example = ".")
    public Set<String> decimalSeparator;

    @Schema(description = "Number of decimal places.", example = "0")
    public Integer decimalPlaces;


}
