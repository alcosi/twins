package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
@Schema(name = TwinFieldLastChangeSearchRangeDTOv1.KEY, description = "Search by last field change timestamp using explicit range")
public class TwinFieldLastChangeSearchRangeDTOv1 implements TwinFieldSearchDTOv1 {

    public static final String KEY = "TwinFieldLastChangeSearchRangeV1";

    @Override
    public String type() {
        return KEY;
    }

    @Schema(description = "Field last change time less then or equals given timestamp")
    public LocalDateTime lessThenOrEquals;

    @Schema(description = "Field last change time greater then or equals given timestamp")
    public LocalDateTime moreThenOrEquals;

    @Schema(description = "Field last change time equals to given timestamp")
    public LocalDateTime equals;
}

