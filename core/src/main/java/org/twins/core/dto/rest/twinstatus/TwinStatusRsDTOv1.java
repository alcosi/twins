package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "TwinStatusRsV1")
public class TwinStatusRsDTOv1 extends Response {
    @Schema(description = "twin status")
    public TwinStatusDTOv2 twinStatus;
}
