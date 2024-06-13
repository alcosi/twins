package org.twins.core.dto.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.twins.core.domain.ReplaceOperation.Strategy;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Schema
@Data
public class ReplaceOperationDTOv1 {
    @Schema(description = "what should be done with old values, if no replacement was given")
    public Strategy strategy = Strategy.deleteIfMissed;

    @Schema(description = "map [old_id -> new_id]")
    public Map<UUID, UUID> replaceMap;

    @Schema(description = "values that can be deleted")
    public Set<UUID> deleteSet;

}
