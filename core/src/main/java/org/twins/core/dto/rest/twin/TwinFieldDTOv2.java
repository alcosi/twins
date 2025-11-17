package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "TwinFieldV2")
public class TwinFieldDTOv2 {
    @Schema
    public String key;

    @Schema
    public String value;
}