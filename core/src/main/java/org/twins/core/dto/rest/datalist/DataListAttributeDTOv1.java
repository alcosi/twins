package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

@Data
@Accessors(chain = true)
@Schema(name = "DataListAttributeV1")
public class DataListAttributeDTOv1 {
    @Schema(description = "key", example = DTOExamples.DATA_LIST_ATTRIBUTE_KEY)
    public String key;

    @Schema(description = "name", example = DTOExamples.NAME)
    public String name;
}
