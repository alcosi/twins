package org.twins.core.dto.rest.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;
import org.twins.core.enums.history.HistoryType;

import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "HistorySearchV1")
public class HistorySearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "twin id list")
    public Set<UUID> twinIdList;

    @Schema(description = "twin id exclude list")
    public Set<UUID> twinIdExcludeList;

    @Schema(description = "need include direct children", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean includeDirectChildren  = false;

    @Schema(description = "actor user id list")
    public Set<UUID> actorUserIdList;

    @Schema(description = "actor user id exclude list")
    public Set<UUID> actorUserIdExcludeList;

    @Schema(description = "type list")
    public Set<HistoryType> typeList;

    @Schema(description = "type exclude list")
    public Set<HistoryType> typeExcludeList;

    @Schema(description = "twin class field id list")
    public Set<UUID> twinClassFieldIdList;

    @Schema(description = "twin class field id exclude list")
    public Set<UUID> twinClassFieldIdExcludeList;

    @Schema(description = "create at range")
    public DataTimeRangeDTOv1 createdAt;

    public HistorySearchDTOv1 addIdListItem(UUID item) {
        this.idList = CollectionUtils.safeAdd(this.idList, item);
        return this;
    }

    public HistorySearchDTOv1 addIdExcludeListItem(UUID item) {
        this.idExcludeList = CollectionUtils.safeAdd(this.idExcludeList, item);
        return this;
    }

    public HistorySearchDTOv1 addTwinIdListItem(UUID item) {
        this.twinIdList = CollectionUtils.safeAdd(this.twinIdList, item);
        return this;
    }

    public HistorySearchDTOv1 addTwinIdExcludeListItem(UUID item) {
        this.twinIdExcludeList = CollectionUtils.safeAdd(this.twinIdExcludeList, item);
        return this;
    }

    public HistorySearchDTOv1 addActorUseridListItem(UUID item) {
        this.actorUserIdList = CollectionUtils.safeAdd(this.actorUserIdList, item);
        return this;
    }

    public HistorySearchDTOv1 addActorUserIdExcludeListItem(UUID item) {
        this.actorUserIdExcludeList = CollectionUtils.safeAdd(this.actorUserIdExcludeList, item);
        return this;
    }

    public HistorySearchDTOv1 addTypeListItem(HistoryType item) {
        this.typeList = CollectionUtils.safeAdd(this.typeList, item);
        return this;
    }

    public HistorySearchDTOv1 addTypeExcludeListItem(HistoryType item) {
        this.typeExcludeList = CollectionUtils.safeAdd(this.typeExcludeList, item);
        return this;
    }
}