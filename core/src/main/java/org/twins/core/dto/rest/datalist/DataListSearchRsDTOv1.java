package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListSearchRsV1")
public class DataListSearchRsDTOv1 extends Response {
    @Schema(description = "results - data lists list")
    public List<DataListDTOv1> dataListList;
}
