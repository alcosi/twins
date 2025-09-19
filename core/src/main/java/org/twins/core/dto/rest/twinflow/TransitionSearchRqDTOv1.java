package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.enums.twinflow.TwinflowTransitionType;
import org.twins.core.dto.rest.Request;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TransitionSearchRqV1")
public class TransitionSearchRqDTOv1 extends Request {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "name like list")
    public Set<String> nameLikeList;

    @Schema(description = "name not like list")
    public Set<String> nameNotLikeList;

    @Schema(description = "description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "description not like list")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "twin class id map")
    public Map<UUID, Boolean> twinClassIdMap;

    @Schema(description = "twin class id exclude map")
    public Map<UUID, Boolean> twinClassIdExcludeMap;

    @Schema(description = "twinflow id list")
    public Set<UUID> twinflowIdList;

    @Schema(description = "twinflow id exclude list")
    public Set<UUID> twinflowIdExcludeList;

    @Schema(description = "source status id list")
    public Set<UUID> srcStatusIdList;

    @Schema(description = "source status id exclude list")
    public Set<UUID> srcStatusIdExcludeList;

    @Schema(description = "destination status id list")
    public Set<UUID> dstStatusIdList;

    @Schema(description = "destination status id exclude list")
    public Set<UUID> dstStatusIdExcludeList;

    @Schema(description = "alias like list")
    public Set<String> aliasLikeList;

    @Schema(description = "permission id list")
    public Set<UUID> permissionIdList;

    @Schema(description = "permission id exclude list")
    public Set<UUID> permissionIdExcludeList;

    @Schema(description = "inbuilt twin factory id list")
    public Set<UUID> inbuiltTwinFactoryIdList;

    @Schema(description = "inbuilt twin factory id exclude list")
    public Set<UUID> inbuiltTwinFactoryIdExcludeList;

    @Schema(description = "drafting twin factory id list")
    public Set<UUID> draftingTwinFactoryIdList;

    @Schema(description = "drafting twin factory id exclude list")
    public Set<UUID> draftingTwinFactoryIdExcludeList;

    @Schema(description = "twinflow transition type id list")
    public Set<TwinflowTransitionType> twinflowTransitionTypeList;

    @Schema(description = "twinflow transition type id exclude list")
    public Set<TwinflowTransitionType> twinflowTransitionTypeExcludeList;}
