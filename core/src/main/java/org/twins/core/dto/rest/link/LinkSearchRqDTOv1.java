package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.enums.link.LinkType;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "LinkSearchRqV1")
public class LinkSearchRqDTOv1 extends Request {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "src twin class id list")
    public Set<UUID> srcTwinClassIdList;

    @Schema(description = "src twin class id exclude list")
    public Set<UUID> srcTwinClassIdExcludeList;

    @Schema(description = "dst twin class id list")
    public Set<UUID> dstTwinClassIdList;

    @Schema(description = "dst twin class id exclude list")
    public Set<UUID> dstTwinClassIdExcludeList;

    @Schema(description = "src or dst twin class id list")
    public Set<UUID> srcOrDstTwinClassIdList;

    @Schema(description = "src or dst twin class id exclude list")
    public Set<UUID> srcOrDstTwinClassIdExcludeList;

    @Schema(description = "forward name like list")
    public Set<String> forwardNameLikeList;

    @Schema(description = "forward name not like list")
    public Set<String> forwardNameNotLikeList;

    @Schema(description = "backward name like list")
    public Set<String> backwardNameLikeList;

    @Schema(description = "backward name not like list")
    public Set<String> backwardNameNotLikeList;

    @Schema(description = "type like list")
    public Set<LinkType> typeLikeList;

    @Schema(description = "type not like list")
    public Set<LinkType> typeNotLikeList;

    @Schema(description = "strength like list")
    public Set<LinkStrength> strengthLikeList;

    @Schema(description = "strength not like list")
    public Set<LinkStrength> strengthNotLikeList;
}
