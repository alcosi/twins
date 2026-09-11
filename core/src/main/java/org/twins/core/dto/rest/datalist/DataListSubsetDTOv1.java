package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DataListSubsetV1")
public class DataListSubsetDTOv1 {

    @Schema(description = "data list subset id")
    private UUID id;

    @Schema(description = "data list id")
    private UUID dataListId;

    @Schema(description = "data list subset name")
    private String name;

    @Schema(description = "data list subset description")
    private String description;

    @Schema(description = "data list subset key")
    private String key;
}
