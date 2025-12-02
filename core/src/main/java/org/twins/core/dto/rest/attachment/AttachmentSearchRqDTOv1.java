package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;
import org.twins.core.dto.rest.LongRangeDTOv1;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "AttachmentSearchRqV1")
public class AttachmentSearchRqDTOv1 extends Request {
    @Schema(description = "id list")
    public Set<UUID> idList;
    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;
    @Schema(description = "twin id list")
    public Set<UUID> twinIdList;
    @Schema(description = "twin id exclude list")
    public Set<UUID> twinIdExcludeList;
    @Schema(description = "twinflow transition id list")
    public Set<UUID> twinflowTransitionIdList;
    @Schema(description = "twinflow transition id exclude list")
    public Set<UUID> twinflowTransitionIdExcludeList;
    @Schema(description = "comment id list")
    public Set<UUID> commentIdList;
    @Schema(description = "comment id exclude list")
    public Set<UUID> commentIdExcludeList;
    @Schema(description = "twin class field id list")
    public Set<UUID> twinClassFieldIdList;
    @Schema(description = "twin class field id exclude list")
    public Set<UUID> twinClassFieldIdExcludeList;
    @Schema(description = "storage link like list")
    public Set<String> storageLinkLikeList;
    @Schema(description = "storage link not like list")
    public Set<String> storageLinkNotLikeList;
    @Schema(description = "view permission id list")
    public Set<UUID> viewPermissionIdList;
    @Schema(description = "view permission id exclude list")
    public Set<UUID> viewPermissionIdExcludeList;
    @Schema(description = "created by user id list")
    public Set<UUID> createdByUserIdList;
    @Schema(description = "created by user id exclude list")
    public Set<UUID> createdByUserIdExcludeList;
    @Schema(description = "external id like list")
    public Set<String> externalIdLikeList;
    @Schema(description = "external id not like list")
    public Set<String> externalIdNotLikeList;
    @Schema(description = "title like list")
    public Set<String> titleLikeList;
    @Schema(description = "title not like list")
    public Set<String> titleNotLikeList;
    @Schema(description = "description like list")
    public Set<String> descriptionLikeList;
    @Schema(description = "description not like list")
    public Set<String> descriptionNotLikeList;
    @Schema(description = "createdAt")
    public DataTimeRangeDTOv1 createdAt;
    @Schema(description = "order")
    public LongRangeDTOv1 order;
}
