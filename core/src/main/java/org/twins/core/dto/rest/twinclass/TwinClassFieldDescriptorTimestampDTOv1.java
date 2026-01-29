package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorTimestampV1")
public class TwinClassFieldDescriptorTimestampDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "timestampV1";
    @Override
    public String fieldType() {
        return KEY;
    }

    @Schema(description = "Timestamp pattern (default: yyyy-MM-dd'T'HH:mm:ss)")
    public String pattern;

    @Schema(description = "[option] Acceptable minimum timestamp value")
    public LocalDateTime beforeDate;

    @Schema(description = "[option] Acceptable maximum timestamp value")
    public LocalDateTime afterDate;
}
