package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListOptionRsV3")
public class DataListOptionRsDTOv3 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "data lists option")
    public DataListOptionDTOv1 option;
}
