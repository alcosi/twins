package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseCountDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinStatusTriggerCountRsV1")
public class TwinStatusTriggerCountRsDTOv1 extends ResponseCountDTOv1 {
    @Schema(description = "count results grouped by requested fields")
    public List<TwinStatusTriggerCountDTOv1> counts;
}
