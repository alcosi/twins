package org.twins.core.dto.rest.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.history.change.HistoryContextDTO;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name = "HistoryV2")
public class HistoryDTOv2 extends HistoryBaseDTOv2 {
    @Schema(description = "Detailed description for history item type. Contains markdown")
    public String typeDescription;

    @Schema(description = "From value description. Contains markdown", example = "")
    public String fromValueDescription;

    @Schema(description = "To value description. Contains markdown", example = "")
    public String toValueDescription;

    @Schema(description = "context")
    public HistoryContextDTO context;
}
