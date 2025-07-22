package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "TwinClassFieldSearchConfiguredRqV2")
public class TwinClassFieldSearchConfiguredRqDTOv1 extends Request {
    @Schema(description = "search narrow")
    public TwinClassFieldSearchDTOv1 narrow;
}
