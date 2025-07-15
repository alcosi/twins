package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.draft.DraftDTOv1;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinTransitionPerformResultMajorRsV1")
public class TwinTransitionPerformResultMajorDTOv1 implements TwinTransitionPerformResultDTO {

    public static final String KEY = "major";

    public TwinTransitionPerformResultMajorDTOv1() {
        this.resultType = KEY;
    }

    @Schema(description = "Result type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String resultType;

    @Schema(description = "draft")
    public DraftDTOv1 draft;
}
