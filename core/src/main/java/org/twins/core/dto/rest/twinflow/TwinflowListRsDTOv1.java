package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinflowListRsV1")
public class TwinflowListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - twinflow list")
    public List<TwinflowBaseDTOv1> twinflowList;
}
