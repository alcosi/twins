package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "DataListOptionV2")
public class DataListOptionDTOv2 extends DataListOptionDTOv1 {
    @Schema(description = "id", example = DTOExamples.DATA_LIST_ID)
    public UUID dataListId;
}
