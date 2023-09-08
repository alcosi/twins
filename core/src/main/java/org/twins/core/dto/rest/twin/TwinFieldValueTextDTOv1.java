package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinFieldValueTextDTOv1")
public class TwinFieldValueTextDTOv1 implements TwinFieldValueDTO {
    public static final String KEY = "textV1";
    public String fieldType = KEY;

    @Schema(description = "Some simple text", example = "Hello world")
    public String text;
}
