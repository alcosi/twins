package org.twins.core.dto.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "IntegerRangev1")
public class IntegerRangeDTOv1 {

    @Schema(description = "from")
    public Integer from;

    @Schema(description = "to")
    public Integer to;
}
