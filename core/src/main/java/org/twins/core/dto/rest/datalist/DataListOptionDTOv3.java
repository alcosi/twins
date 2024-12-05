package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListOptionV3")
public class DataListOptionDTOv3 extends DataListOptionDTOv2 {
    @Schema(description = "data list")
    public DataListDTOv1 dataList;

    @Schema(description = "business account")
    public BusinessAccountDTOv1 businessAccount;
}
