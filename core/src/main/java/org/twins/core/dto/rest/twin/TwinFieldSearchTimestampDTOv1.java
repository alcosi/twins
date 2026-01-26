package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Schema(name = TwinFieldSearchTimestampDTOv1.KEY, description = "(beforeDate & afterDate connected with AND) and then connected to equals with OR and to empty with OR")
public class TwinFieldSearchTimestampDTOv1 implements TwinFieldSearchDTOv1 {

    public static final String KEY = "TwinFieldSearchTimestampV1";
    @Override
    public String type() {
        return KEY;
    }

    @Schema(description = "Twin field timestamp before given timestamp")
    public LocalDateTime beforeDate;

    @Schema(description = "Twin field timestamp after given timestamp")
    public LocalDateTime afterDate;

    @Schema(description = "Twin field timestamp equals to given timestamp")
    public LocalDateTime equals;
}
