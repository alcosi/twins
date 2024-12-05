package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "DataListOptionV1")
public class DataListOptionDTOv1 {
    @Schema(description = "id", example = DTOExamples.DATA_LIST_OPTION_ID)
    public UUID id;

    @Schema(description = "name", example = "Bharat")
    public String name;

    @Schema(description = "icon", example = "Icon path")
    public String icon;

    @Schema(description = "description", defaultValue = "false", example = "Option is currently in active")
    public Boolean disabled;

    @Schema(description = "attributes")
    public Map<String, String> attributes;
}
