package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "DataListOptionV1")
public class DataListOptionDTOv1 {
    @Schema(description = "id", example = "c2a7f81f-d7da-43e8-a1d3-18d6f632878b")
    public UUID id;

    @Schema(description = "name", example = "Country list")
    public String name;

    @Schema(description = "description", defaultValue = "false", example = "Option is currently in active")
    public Boolean disabled;
}
