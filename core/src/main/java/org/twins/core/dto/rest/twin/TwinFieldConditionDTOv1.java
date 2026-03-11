package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinFieldConditionV1")
public class TwinFieldConditionDTOv1 {
    @Schema(description = "twin class field id")
    public UUID twinClassFieldId;

    @Schema(description = "twinFieldSearch")
    public TwinFieldSearchDTOv1 twinFieldSearch;
}
