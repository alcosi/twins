package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.enums.link.LinkType;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "LinkSearchDTOv1")
public class LinkSearchDTOv1 {
    @Size(max = 50)
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Size(max = 50)
    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Size(max = 50)
    @Schema(description = "src twin class id list")
    public Set<UUID> srcTwinClassIdList;

    @Size(max = 50)
    @Schema(description = "src twin class id exclude list")
    public Set<UUID> srcTwinClassIdExcludeList;

    @Schema(description = "src twin class inheritable", example = DTOExamples.TERNARY)
    public Ternary srcTwinClassInheritable;

    @Size(max = 50)
    @Schema(description = "dst twin class id list")
    public Set<UUID> dstTwinClassIdList;

    @Size(max = 50)
    @Schema(description = "dst twin class id exclude list")
    public Set<UUID> dstTwinClassIdExcludeList;

    @Schema(description = "dst twin class inheritable", example = DTOExamples.TERNARY)
    public Ternary dstTwinClassInheritable;

    @Size(max = 50)
    @Schema(description = "src or dst twin class id list")
    public Set<UUID> srcOrDstTwinClassIdList;

    @Size(max = 50)
    @Schema(description = "src or dst twin class id exclude list")
    public Set<UUID> srcOrDstTwinClassIdExcludeList;

    @Size(max = 50)
    @Schema(description = "forward name like list")
    public Set<String> forwardNameLikeList;

    @Size(max = 50)
    @Schema(description = "forward name not like list")
    public Set<String> forwardNameNotLikeList;

    @Size(max = 50)
    @Schema(description = "backward name like list")
    public Set<String> backwardNameLikeList;

    @Size(max = 50)
    @Schema(description = "backward name not like list")
    public Set<String> backwardNameNotLikeList;

    @Size(max = 50)
    @Schema(description = "type like list")
    public Set<LinkType> typeLikeList;

    @Size(max = 50)
    @Schema(description = "type not like list")
    public Set<LinkType> typeNotLikeList;

    @Size(max = 50)
    @Schema(description = "strength like list")
    public Set<LinkStrength> strengthLikeList;

    @Size(max = 50)
    @Schema(description = "strength not like list")
    public Set<LinkStrength> strengthNotLikeList;
}
