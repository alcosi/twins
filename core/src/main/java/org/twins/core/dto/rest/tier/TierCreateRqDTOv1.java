package org.twins.core.dto.rest.tier;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TierCreateRqV1")
public class TierCreateRqDTOv1 extends Request {
    @Valid
    @Schema(description = "tier create")
    public TierCreateDTOv1 tier;
}