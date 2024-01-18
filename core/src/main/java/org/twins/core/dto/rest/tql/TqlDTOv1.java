package org.twins.core.dto.rest.tql;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TqlDTOv1")
public class TqlDTOv1 {
    @Schema(description = "id", example = "c2a7f81f-d7da-43e8-a1d3-18d6f632878b")
    public UUID id;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;


}
