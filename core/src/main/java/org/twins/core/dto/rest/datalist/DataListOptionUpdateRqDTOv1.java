package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.enums.datalist.DataListStatus;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Deprecated
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListOptionUpdateRqV1")
public class DataListOptionUpdateRqDTOv1 extends DataListOptionSaveRqDTOv1 {
    @Schema(description = "data list id", example = DTOExamples.DATA_LIST_ID)
    public UUID dataListId;

    @Schema(description = "status", example = DTOExamples.DATA_LIST_OPTION_STATUS)
    public DataListStatus status;
}
