package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListOptionCreateDV1")
public class DataListOptionCreateDTOv1 extends DataListOptionSaveDTOv1 {
    @Schema(description = "data list id", example = DTOExamples.DATA_LIST_ID)
    public UUID dataListId;
}
