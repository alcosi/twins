package org.twins.core.dto.rest.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;
import org.twins.core.enums.history.HistoryType;

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

    public HistorySearchRqDTOv1 addIdListItem(UUID item) {
        this.idList = CollectionUtils.safeAdd(this.idList, item);
        return this;
    }

    public HistorySearchRqDTOv1 addIdExcludeListItem(UUID item) {
        this.idExcludeList = CollectionUtils.safeAdd(this.idExcludeList, item);
        return this;
    }

    public HistorySearchRqDTOv1 addTwinIdListItem(UUID item) {
        this.twinIdList = CollectionUtils.safeAdd(this.twinIdList, item);
        return this;
    }

    public HistorySearchRqDTOv1 addTwinIdExcludeListItem(UUID item) {
        this.twinIdExcludeList = CollectionUtils.safeAdd(this.twinIdExcludeList, item);
        return this;
    }

    public HistorySearchRqDTOv1 addActorUseridListItem(UUID item) {
        this.actorUseridList = CollectionUtils.safeAdd(this.actorUseridList, item);
        return this;
    }

    public HistorySearchRqDTOv1 addActorUserIdExcludeListItem(UUID item) {
        this.actorUserIdExcludeList = CollectionUtils.safeAdd(this.actorUserIdExcludeList, item);
        return this;
    }

    public HistorySearchRqDTOv1 addTypeListItem(HistoryType item) {
        this.typeList = CollectionUtils.safeAdd(this.typeList, item);
        return this;
    }

    public HistorySearchRqDTOv1 addTypeExcludeListItem(HistoryType item) {
        this.typeExcludeList = CollectionUtils.safeAdd(this.typeExcludeList, item);
        return this;
    }
}
