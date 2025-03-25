package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import static org.twins.core.dto.rest.twin.TwinFieldSearchNumericDTOv1.KEY;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(fluent = true)
@Schema(name = KEY, description = "(less & more connected with AND) and after connected to equals with OR")
public class TwinFieldSearchNumericDTOv1 extends TwinFieldSearchDTOv1 {

    public static final String KEY = "searchNumericValueV1";

    @JsonProperty("type")
    public String type = KEY;

    @Schema(description = "Twin field numeric value less then given")
    public String lessThen;

    @Schema(description = "Twin field numeric value greater then given")
    public String moreThen;

    @Schema(description = "Twin field numeric value equals to given")
    public String equals;

}
