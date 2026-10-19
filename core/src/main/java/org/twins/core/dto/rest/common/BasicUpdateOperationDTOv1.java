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

    @Schema(description = "what should be done with old values that have no replacement in replaceMap. " +
            "restrict = reject the update (an explicit replaceMap entry is required for every old value); " +
            "delete = drop the old value. For a mandatory scalar field stored on the entity itself " +
            "(e.g. flavor) 'delete' cannot just clear the value, so the entities holding an obsolete " +
            "unmapped value (their twins) are deleted; entities that simply lack a value are back-filled " +
            "from the NULLIFY_MARKER default of replaceMap instead")
    public EntityRelinkOperationStrategy onUnreplacedStrategy = EntityRelinkOperationStrategy.delete;

    @Schema(description = "map [old_id -> new_id]. A special key ffffffff-ffff-ffff-ffff-ffffffffffff " +
            "(NULLIFY_MARKER) denotes the default value applied to entities that currently have none " +
            "(e.g. twins without a flavor when a mandatory flavor list is enabled on their class). " +
            "Mapping an old_id to NULLIFY_MARKER requests deletion of the entities holding that old value")
    public Map<UUID, UUID> replaceMap;

}
