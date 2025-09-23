package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.enums.datalist.DataListStatus;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListOptionUpdateV1")
public class DataListOptionUpdateDTOv1 extends DataListOptionSaveDTOv1 {
    @Schema(description = "data list option id", example = DTOExamples.DATA_LIST_OPTION_ID)
    public UUID id;

    @Schema(description = "data list id", example = DTOExamples.DATA_LIST_ID)
    public UUID dataListId;

    @Schema(description = "status", example = DTOExamples.DATA_LIST_OPTION_STATUS)
    public DataListStatus status;
}
