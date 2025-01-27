package org.twins.core.dto.rest.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "HistoryViewRsV1")
public class HistoryViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "history")
    public HistoryDTOv1 history;
}
