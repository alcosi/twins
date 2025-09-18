package org.twins.core.dto.rest.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.domain.enum_.history.HistoryType;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "HistorySearchRqV1")
public class HistorySearchRqDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "twin id list")
    public Set<UUID> twinIdList;

    @Schema(description = "twin id exclude list")
    public Set<UUID> twinIdExcludeList;

    @Schema(description = "need include direct children", example = DTOExamples.BOOLEAN_TRUE)
    public boolean includeDirectChildren = false;

    @Schema(description = "actor user id list")
    public Set<UUID> actorUseridList;

    @Schema(description = "actor user id exclude list")
    public Set<UUID> actorUserIdExcludeList;

    @Schema(description = "type list")
    public Set<HistoryType> typeList;

    @Schema(description = "type exclude list")
    public Set<HistoryType> typeExcludeList;

    @Schema(description = "create at")
    public DataTimeRangeDTOv1 createdAt;
}
