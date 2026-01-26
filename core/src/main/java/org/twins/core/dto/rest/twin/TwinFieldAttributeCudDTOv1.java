package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinFieldAttributeCudV1")
public class TwinFieldAttributeCudDTOv1 {
    @Schema(description = "twin field attribute create list")
    public List<TwinFieldAttributeCreateDTOv1> creates;

    @Schema(description = "twin field attribute update list")
    public List<TwinFieldAttributeUpdateDTOv1> updates;

    @Schema(description = "twin field attribute id delete set")
    public Set<UUID> deletes;
}
