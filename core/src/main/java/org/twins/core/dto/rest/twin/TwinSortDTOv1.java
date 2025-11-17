package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.query.SortDirection;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinSortV1")
public class TwinSortDTOv1 {
    @Schema(description = "Twin class field id to sort by")
    private UUID twinClassFieldId;

    @Schema(description = "Sort direction: ASC or DESC")
    private SortDirection direction;
}
