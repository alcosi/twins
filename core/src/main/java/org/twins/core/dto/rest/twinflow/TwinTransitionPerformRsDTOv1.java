package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.dto.rest.twin.TwinDTOv2;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinTransitionPerformRsV1")
public class TwinTransitionPerformRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "list of twins from input")
    public List<TwinDTOv2> transitionedTwinList;

    @Schema(description = "list of twins processed by transition (some new can be created or updated). Key is twinClassId")
    public Map<UUID, List<TwinDTOv2>> processedTwinList;
}
