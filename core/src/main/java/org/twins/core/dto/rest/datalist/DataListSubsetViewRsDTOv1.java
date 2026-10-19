package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListSubsetViewRsV1")
public class DataListSubsetViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - data list subset")
    public DataListSubsetDTOv1 dataListSubset;
}
