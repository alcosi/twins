package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "DataListV1")
public class DataListDTOv1 {
    @Schema(description = "id", example = DTOExamples.DATA_LIST_ID)
    public UUID id;

    @Schema(description = "name", example = "Country list")
    public String name;

    @Schema(description = "description", example = "Supported country list")
    public String description;

    @Schema(description = "updated at", example = DTOExamples.INSTANT)
    public Instant updatedAt;

    @Schema(description = "List options")
    public Map<UUID, DataListOptionDTOv1> options;
}
