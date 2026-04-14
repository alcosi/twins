package org.twins.core.dto.rest.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "HistorySearchRqV1")
public class HistorySearchRqDTOv1 extends Request {
    @Schema(description = "search params")
    public HistorySearchDTOv1 search;
}
