package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinCountBySearchRsV1")
public class TwinCountBySearchRsDTOv1 extends Response {
    @Schema(description = "twins count matching the saved search")
    public Long count;
}
