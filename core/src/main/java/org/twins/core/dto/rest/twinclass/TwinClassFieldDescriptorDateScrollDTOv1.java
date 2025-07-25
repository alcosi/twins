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
    @Override
    public String fieldType() {
        return KEY;
    }

    @Schema(description = "Date pattern (default: yyyy-MM-ddTHH:mm:ss)")
    public String pattern;

    @Schema(description = "[option] Acceptable minimum date value")
    public LocalDateTime beforeDate;

    @Schema(description = "[option] Acceptable maximum date value")
    public LocalDateTime afterDate;
}
