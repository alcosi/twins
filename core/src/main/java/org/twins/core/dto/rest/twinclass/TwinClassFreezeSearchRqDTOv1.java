package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassFreezeSearchRqV1")
public class TwinClassFreezeSearchRqDTOv1 extends Request {
    @Schema
    public TwinClassFreezeSearchDTOv1 search;
}
