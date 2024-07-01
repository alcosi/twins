package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListOptionRsV1")
public class DataListOptionRsDTOv1 extends Response {
    @Schema(description = "id", example = DTOExamples.DATA_LIST_ID)
    public UUID dataListId;

    @Schema(description = "data lists option")
    public DataListOptionDTOv1 option;
}
