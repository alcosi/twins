package org.twins.core.dto.rest.tql;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TqlDTOv1")
public class TqlDTOv1 {
    @Schema(description = "id", example = "c2a7f81f-d7da-43e8-a1d3-18d6f632878b")
    public UUID id;

    @Schema(description = "created at", example = "1549632759")
    public Instant createdAt;


}
