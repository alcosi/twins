package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DataListSubsetSaveV1")
public class DataListSubsetSaveDTOv1 {
    @Schema(description = "Data list id. Immutable after creation")
    public UUID dataListId;

    @Schema(description = "Data list subset name")
    public String name;

    @Schema(description = "Data list subset description")
    public String description;

    @Schema(description = "Data list subset key. Unique within the data list")
    public String key;
}
