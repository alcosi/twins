package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinflowBaseV2")
public class TwinflowBaseDTOv2 extends TwinflowBaseDTOv1{
    @Schema(description = "initial status")
    public TwinStatusDTOv1 initialStatus;

    @Schema(description = "twinflow author")
    public UserDTOv1 createdByUser;
}
