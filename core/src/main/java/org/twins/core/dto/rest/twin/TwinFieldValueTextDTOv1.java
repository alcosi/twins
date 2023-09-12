package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name =  "TwinFieldValueTextV1")
public class TwinFieldValueTextDTOv1 extends TwinFieldValueDTO {
    public static final String KEY = "textV1";
    public String valueType = KEY;

    @Schema(description = "Some simple text", example = "Hello world")
    public String text;
}
