package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "TwinClassFieldSearchRqV2")
public class TwinClassFieldSearchRqDTOv2 extends Request {
    @Schema(description = "search")
    public TwinClassFieldSearchDTOv1 search;
}
