package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListOptionListRsV1")
public class DataListOptionListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "data list options")
    public List<DataListOptionDTOv1> options;
}
