package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name = TwinFieldLastChangeSearchBeforeDTOv1.KEY, description = "Search by last field change timestamp relative time")
public class TwinFieldLastChangeSearchBeforeDTOv1 implements TwinFieldSearchDTOv1 {

    public static final String KEY = "TwinFieldLastChangeSearchBeforeV1";

    @Override
    public String type() {
        return KEY;
    }

    @Schema(description = "Changed less than N seconds ago (N > 0)")
    public long lessThenSecondsAgo;
}

