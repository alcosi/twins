package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinStatusTriggerListRsV1")
public class TwinStatusTriggerListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "twin status triggers")
    public List<TwinStatusTriggerDTOv1> twinStatusTriggers;
}
