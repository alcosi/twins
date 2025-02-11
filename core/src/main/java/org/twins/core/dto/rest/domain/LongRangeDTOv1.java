package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "LongRangeDTOv1")
public class LongRangeDTOv1 {

    @Schema(description = "from")
    public Long from;

    @Schema(description = "to")
    public Long to;
}
