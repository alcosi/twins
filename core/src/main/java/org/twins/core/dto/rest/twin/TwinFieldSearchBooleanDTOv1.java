package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name = TwinFieldSearchBooleanDTOv1.KEY, oneOf = { TwinFieldSearchDTOv1.class })
public class TwinFieldSearchBooleanDTOv1 implements TwinFieldSearchDTOv1 {

    public static final String KEY = "TwinFieldSearchBooleanV1";

    public TwinFieldSearchBooleanDTOv1() {
        this.type = KEY;
    }

    @Schema(description = "Search type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String type;

    @Schema(description = "include entities with empty or null values to result")
    public Boolean value;
}
