package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twin.TwinStatusDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinStatusCreateRsV1")
public class TwinStatusCreateRsDTOv1 extends TwinStatusSaveRsDTOv1 {
    @Schema(description = "result - twin status")
    public TwinStatusDTOv1 twinStatus;
}
