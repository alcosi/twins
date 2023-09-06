package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinClassListRqV1")
public class TwinClassSearchRqDTOv1 extends Request {
    @Schema(description = "twin class id list", example = "")
    public List<UUID> twinClassIdList;

    @Schema(description = "twin class show mode", example = TwinClassRestDTOMapper.Mode._SHOW_FIELDS)
    public TwinClassRestDTOMapper.Mode showTwinClassMode = TwinClassRestDTOMapper.Mode.SHOW_FIELDS;
}
