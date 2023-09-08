package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinFieldValueColorHexDTOv1")
public class TwinFieldValueColorHexDTOv1 implements TwinFieldValueDTO {
    public static final String KEY = "colorHexV1";
    public String fieldType = KEY;

    @Schema(description = "Color hex code", example = "#575584")
    public String hex;
}
