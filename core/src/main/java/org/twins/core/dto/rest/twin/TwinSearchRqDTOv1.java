package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinSearchRqV1", example = "{ \"twinClassIdList\": [ \"ab750e98-70dd-404e-8164-4e0daa187164\" ] } ")
public class TwinSearchRqDTOv1 extends Request {
    @Schema(description = "Twin class id list")
    public List<UUID> twinClassIdList;

    @Schema(description = "Twin class extends id list")
    public List<UUID> extendsTwinClassIdList;

    @Schema(description = "Twin name like list")
    public List<String> twinNameLikeList;

    @Schema(description = "Head twin id list")
    public List<UUID> headTwinIdList;

    @Schema(description = "Twin id list")
    public List<UUID> twinIdList;

    @Schema(description = "Twin id exclude list")
    public List<UUID> twinIdExcludeList;

    @Schema(description = "Status id list")
    public List<UUID> statusIdList;

    @Schema(description = "Assigner id list")
    public List<UUID> assignerUserIdList;

    @Schema(description = "Reporter id list")
    public List<UUID> createdByUserIdList;

    @Schema(description = "Include dst twins with given links")
    public List<TwinSearchByLinkDTOv1> linksList;

    @Schema(description = "Exclude dst twins with given links")
    public List<TwinSearchByLinkDTOv1> noLinksList;

    @Schema(description = "Hierarchy ids filter")
    public List<UUID>  hierarchyTreeContainsIdList;
}
