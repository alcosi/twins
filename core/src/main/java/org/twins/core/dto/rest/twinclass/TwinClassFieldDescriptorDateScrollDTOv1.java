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

    @Schema(description = "[option] Maximum number of days from current date in the past for a valid date range", example = "30")
    public Integer daysPast;

    @Schema(description = "[option] The maximum number of days from the current date in the future for a valid date range", example = "365")
    public Integer daysFuture;
}
