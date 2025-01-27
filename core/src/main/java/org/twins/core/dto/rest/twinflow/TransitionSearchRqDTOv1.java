package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "TransitionSearchRqV1")
public class TransitionSearchRqDTOv1 extends Request {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "twin class id list")
    public List<UUID> twinClassIdList;

    @Schema(description = "twin class id exclude list")
    public List<UUID> twinClassIdExcludeList;

    @Schema(description = "twinflow id list")
    public List<UUID> twinflowIdList;

    @Schema(description = "twinflow id exclude list")
    public List<UUID> twinflowIdExcludeList;

    @Schema(description = "source status id list")
    public List<UUID> srcStatusIdList;

    @Schema(description = "source status id exclude list")
    public List<UUID> srcStatusIdExcludeList;

    @Schema(description = "destination status id list")
    public List<UUID> dstStatusIdList;

    @Schema(description = "destination status id exclude list")
    public List<UUID> dstStatusIdExcludeList;

    @Schema(description = "alias like list")
    public List<String> aliasLikeList;

    @Schema(description = "permission id list")
    public List<UUID> permissionIdList;

    @Schema(description = "permission id exclude list")
    public List<UUID> permissionIdExcludeList;

    @Schema(description = "inbuilt twin factory id list")
    public List<UUID> inbuiltTwinFactoryIdList;

    @Schema(description = "inbuilt twin factory id exclude list")
    public List<UUID> inbuiltTwinFactoryIdExcludeList;

    @Schema(description = "drafting twin factory id list")
    public List<UUID> draftingTwinFactoryIdList;

    @Schema(description = "drafting twin factory id exclude list")
    public List<UUID> draftingTwinFactoryIdExcludeList;
}
