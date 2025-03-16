package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorDateScrollV1")
public class TwinClassFieldDescriptorDateScrollDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "dateScrollV1";
    public String fieldType = KEY;

    @Schema(description = "Date pattern (default: yyyy-MM-ddTHH:mm:ss)")
    public String pattern;

    @Schema(description = "Minimum allowed date (format: yyyy-MM-ddTHH:mm:ss). Null means no lower limit")
    public LocalDateTime fromDate;

    @Schema(description = "Maximum allowed date (format: yyyy-MM-ddTHH:mm:ss). Null means no upper limit")
    public LocalDateTime toDate;
}
