package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name = TwinFieldSearchNumericDTOv1.KEY, description = "(less & more connected with AND) and after connected to equals with OR")
public class TwinFieldSearchNumericDTOv1 implements TwinFieldSearchDTOv1 {
    public static final String KEY = "TwinFieldSearchNumericV1";
    @Override
    public String type() {
        return KEY;
    }

    @Schema(description = "Twin field numeric value less then given")
    public String lessThen;

    @Schema(description = "Twin field numeric value greater then given")
    public String moreThen;

    @Schema(description = "Twin field numeric value equals to given")
    public String equals;

}
