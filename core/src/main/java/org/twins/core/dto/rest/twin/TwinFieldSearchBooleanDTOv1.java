package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name = TwinFieldSearchBooleanDTOv1.KEY)
public class TwinFieldSearchBooleanDTOv1 implements TwinFieldSearchDTOv1 {

    public static final String KEY = "TwinFieldSearchBooleanV1";
    @Override
    public String type() {
        return KEY;
    }

    @Schema(description = "boolean value to search by (true/false)")
    public Boolean value;

    @Schema(description = "flag to search only null values")
    public Boolean searchByNullValue;
}
