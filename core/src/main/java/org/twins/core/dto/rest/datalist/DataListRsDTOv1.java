package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListRsV1")
public class DataListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - data lists list")
    public DataListDTOv1 dataList;
}
