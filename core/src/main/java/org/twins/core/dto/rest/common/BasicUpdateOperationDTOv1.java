package org.twins.core.dto.rest.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.twins.core.enums.EntityRelinkOperationStrategy;

import java.util.Map;
import java.util.UUID;

@Schema
@Data
public class BasicUpdateOperationDTOv1 {
    @Schema(description = "new id. " +
            "Use ffffffff-ffff-ffff-ffff-ffffffffffff for nullify value")
    public UUID newId;

    @Schema(description = "what should be done with old values, if no replacement was given")
    public EntityRelinkOperationStrategy onUnreplacedStrategy = EntityRelinkOperationStrategy.delete;

    @Schema(description = "map [old_id -> new_id]")
    public Map<UUID, UUID> replaceMap;

}
