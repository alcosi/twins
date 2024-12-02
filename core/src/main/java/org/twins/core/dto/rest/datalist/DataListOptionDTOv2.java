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
@Schema(name = "DataListOptionV2")
public class DataListOptionDTOv2 extends DataListOptionDTOv1 {
    @Schema(description = "id", example = DTOExamples.DATA_LIST_ID)
    public UUID dataListId;

    @Schema(description = "business account id", example = DTOExamples.BUSINESS_ACCOUNT_ID)
    public UUID businessAccountId;
}
