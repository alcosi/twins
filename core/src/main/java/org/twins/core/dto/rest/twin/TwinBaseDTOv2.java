package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name =  "TwinBaseV2")
public class TwinBaseDTOv2 extends TwinBaseDTOv1 {
    @Schema(description = "status")
    public TwinStatusDTOv1 status;

    @Schema(description = "class")
    public TwinClassDTOv1 twinClass;

    @Schema(description = "current assigner")
    public UserDTOv1 assignerUser;

    @Schema(description = "author")
    public UserDTOv1 authorUser;

    @Schema(description = "headTwin")
    public TwinBaseDTOv2 headTwin;
}
