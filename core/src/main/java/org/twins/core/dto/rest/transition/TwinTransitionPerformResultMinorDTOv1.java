package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twin.TwinDTOv2;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinTransitionPerformResultMinorRsV1")
public class TwinTransitionPerformResultMinorDTOv1 implements TwinTransitionPerformResultDTO {

    public static final String KEY = "minor";

    public TwinTransitionPerformResultMinorDTOv1() {
        this.resultType = KEY;
    }

    @Schema(description = "Result type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String resultType;

    @Schema(description = "list of twins from input")
    public List<TwinDTOv2> transitionedTwinList;

    @Schema(description = "list of twins processed by transition (some new can be created or updated). Key is twinClassId")
    public Map<UUID, List<TwinDTOv2>> processedTwinList;
}
