package org.twins.core.dto.rest.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "HistoryTypeV1")
public class HistoryTypeDTOv1 {
    @Schema(description = "history type id")
    public String id;

    @Schema(description = "snapshot message template")
    public String snapshotMessageTemplate;
}
