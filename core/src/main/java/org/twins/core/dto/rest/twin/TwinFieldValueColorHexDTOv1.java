package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name =  "TwinFieldValueColorHexV1")
public class TwinFieldValueColorHexDTOv1 extends TwinFieldValueDTO {
    public static final String KEY = "colorHexV1";
    public String valueType = KEY;

    @Schema(description = "Color hex code", example = "#575584")
    public String hex;
}
