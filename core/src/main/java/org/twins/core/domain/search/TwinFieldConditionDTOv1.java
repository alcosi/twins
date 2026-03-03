package org.twins.core.domain.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.twins.core.dto.rest.twin.TwinFieldSearchDTOv1;

import java.util.UUID;

@Data
@Schema(name =  "TwinFieldConditionV1")
public class TwinFieldConditionDTOv1 {
    @Schema(description = "id")
    public UUID twinClassFieldId;
    @Schema(description = "twinFieldSearch")
    public TwinFieldSearchDTOv1 twinFieldSearch;
}
