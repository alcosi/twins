package org.twins.core.dto.rest;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "DataTimeRangeV1")
public class DataTimeRangeDTOv1 {
    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "data time form", example = DTOExamples.INSTANT)
    public LocalDateTime from;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "data time to", example = DTOExamples.INSTANT)
    public LocalDateTime to;
}
