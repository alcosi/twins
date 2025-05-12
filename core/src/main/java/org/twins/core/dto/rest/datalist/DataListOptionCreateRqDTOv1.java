package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Deprecated
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListOptionCreateRqDV1")
public class DataListOptionCreateRqDTOv1 extends DataListOptionSaveRqDTOv1 {
    @Schema(description = "data list id", example = DTOExamples.DATA_LIST_ID)
    public UUID dataListId;
}
