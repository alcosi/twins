package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Schema(name = "TwinTriggerListRsV1")
@Accessors(chain = true)
public class TwinTriggerListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "twin triggers")
    public List<TwinTriggerDTOv1> triggers;
}
