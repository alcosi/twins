package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinLinkSearchRqV1")
public class TwinLinkSearchRqDTOv1 extends Request {
    @Schema(description = "twin link search")
    public TwinLinkSearchDTOv1 twinkLinkSearch;
}
