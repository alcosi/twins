package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.enums.datalist.DataListStatus;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "DataListOptionCountV1")
public class DataListOptionCountDTOv1 extends CountDTOv1 {
    @Schema(description = "data list id", example = DTOExamples.DATA_LIST_ID)
    @RelatedObject(type = DataListDTOv1.class, name = "dataList")
    public UUID dataListId;

    @Schema(description = "business account id", example = DTOExamples.BUSINESS_ACCOUNT_ID)
    @RelatedObject(type = BusinessAccountDTOv1.class, name = "businessAccount")
    public UUID businessAccountId;

    @Schema(description = "data list option status")
    public DataListStatus status;

    @Schema(description = "flag for custom field")
    public Boolean custom;
}
