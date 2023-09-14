package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorDateScrollV1")
public class TwinClassFieldDescriptorDateScrollDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "dateScrollV1";
    public String fieldType = KEY;

    @Schema(description = "Date pattern")
    public String pattern;
}
