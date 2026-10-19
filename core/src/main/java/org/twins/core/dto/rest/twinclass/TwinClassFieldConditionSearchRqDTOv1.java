package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassFieldConditionSearchRqV1")
public class TwinClassFieldConditionSearchRqDTOv1 extends Request {

    @Schema(description = "search data")
    private TwinClassFieldConditionSearchDTOv1 search;
}
