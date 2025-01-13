package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twinclass.TwinClassBaseDTOv1;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "TwinStatusV2")
public class TwinStatusDTOv2 extends TwinStatusDTOv1 {
    @Schema(description = "twin class")
    public TwinClassBaseDTOv1 twinClass;
}
