package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;

import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListRsV1")
public class DataListRsDTOv1 extends Response {
    @Schema(description = "results - data lists list")
    public List<DataListDTOv1> dataListList;
}
