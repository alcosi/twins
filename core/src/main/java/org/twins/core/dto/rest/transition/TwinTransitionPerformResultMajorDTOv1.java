package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.draft.DraftDTOv1;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinTransitionPerformResultMajorRsV1")
public class TwinTransitionPerformResultMajorDTOv1 implements TwinTransitionPerformResultDTO {
    public static final String KEY = "major";
    public String resultType = KEY;

    @Schema(description = "draft")
    public DraftDTOv1 draft;
}
