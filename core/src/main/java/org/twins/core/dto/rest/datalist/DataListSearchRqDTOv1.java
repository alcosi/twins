package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "DataListRqV1")
public class DataListSearchRqDTOv1 extends Request {
    @Schema(description = "data list id list", example = "")
    public List<UUID> dataListIdList;
}
