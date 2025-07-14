package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema
public class TwinFieldSearchItem {
    @Schema(description = "twin class field id")
    public UUID fieldId;

    @Schema(description = "field search polymorph", additionalPropertiesSchema = TwinFieldSearchDTOv1.class)
    public TwinFieldSearchDTOv1 fieldSearch;
}
