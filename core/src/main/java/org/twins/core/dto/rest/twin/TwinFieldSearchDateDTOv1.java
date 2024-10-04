package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinFieldSearchDateV1", description = "(less & more connected with AND) and after connected to equals with OR and to emty with OR")
public class TwinFieldSearchDateDTOv1 extends TwinFieldSearchDTOv1 {

    public static final String KEY = "searchDateValueV1";
    public String type = KEY;

    @Schema(description = "Twin field date less then given date")
    public LocalDateTime lessThen;

    @Schema(description = "Twin field date greater then given date")
    public LocalDateTime moreThen;

    @Schema(description = "Twin field date equals to given date")
    public LocalDateTime equals;

    @Schema(description = "include entities with empty or null values to result")
    boolean empty;

}
