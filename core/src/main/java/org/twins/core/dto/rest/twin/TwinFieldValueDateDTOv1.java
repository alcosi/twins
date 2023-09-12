package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name =  "TwinFieldValueDateV1")
public class TwinFieldValueDateDTOv1 extends TwinFieldValueDTO {
    public static final String KEY = "dateV1";
    public String valueType = KEY;

    @Schema(description = "Date")
    public String date;
}
