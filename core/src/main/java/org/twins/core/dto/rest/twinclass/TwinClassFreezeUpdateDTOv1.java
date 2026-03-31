package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinClassFreezeUpdateV1")
public class TwinClassFreezeUpdateDTOv1 extends TwinClassFreezeSaveDTOv1 {
    @Schema(description = "id")
    public UUID id;
}
