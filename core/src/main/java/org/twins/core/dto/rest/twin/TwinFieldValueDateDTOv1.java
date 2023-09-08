package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinFieldValueDateDTOv1")
public class TwinFieldValueDateDTOv1 implements TwinFieldValueDTO {
    public static final String KEY = "dateV1";
    public String fieldType = KEY;

    @Schema(description = "Date")
    public String date;
}
