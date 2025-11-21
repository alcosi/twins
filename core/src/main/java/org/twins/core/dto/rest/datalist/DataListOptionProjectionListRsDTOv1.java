package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListOptionProjectionListRsV1")
public class DataListOptionProjectionListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "data list option projection list")
    public List<DataListOptionProjectionDTOv1> dataListOptionProjections;
}
