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
@Schema(name = "DataListProjectionRsV1")
public class DataListProjectionRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    public List<DataListProjectionDTOv1> dataListProjections;
}
