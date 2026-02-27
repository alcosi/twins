package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinFactoryTriggerListRsV1")
public class TwinFactoryTriggerListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "twin factory triggers")
    public List<TwinFactoryTriggerDTOv1> twinFactoryTriggers;
}
