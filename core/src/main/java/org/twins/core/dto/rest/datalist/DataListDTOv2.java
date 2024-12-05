package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListV2")
public class DataListDTOv2 extends DataListDTOv1 {
    @Schema(description = "list of option ids")
    public Set<UUID> optionIdList;

    @Schema(description = "list options")
    public Map<UUID, DataListOptionDTOv1> options;
}
