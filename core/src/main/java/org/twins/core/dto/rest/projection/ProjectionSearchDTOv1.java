package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "ProjectionSearchV1")
public class ProjectionSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "src twin pointer id list")
    public Set<UUID> srcTwinPointerIdList;

    @Schema(description = "src twin pointer id exclude list")
    public Set<UUID> srcTwinPointerIdExcludeList;

    @Schema(description = "src twin class field id list")
    public Set<UUID> srcTwinClassFieldIdList;

    @Schema(description = "src twin class field id exclude list")
    public Set<UUID> srcTwinClassFieldIdExcludeList;

    @Schema(description = "dst twin class id list")
    public Set<UUID> dstTwinClassIdList;

    @Schema(description = "dst twin class id exclude list")
    public Set<UUID> dstTwinClassIdExcludeList;

    @Schema(description = "dst twin class field id list")
    public Set<UUID> dstTwinClassFieldIdList;

    @Schema(description = "dst twin class field id exclude list")
    public Set<UUID> dstTwinClassFieldIdExcludeList;

    @Schema(description = "projection type id list")
    public Set<UUID> projectionTypeIdList;

    @Schema(description = "projection type id exclude list")
    public Set<UUID> projectionTypeIdExcludeList;

    @Schema(description = "field projector id list")
    public Set<Integer> fieldProjectorIdList;

    @Schema(description = "field projector id exclude list")
    public Set<Integer> fieldProjectorIdExcludeList;

    @Schema(description = "projections active")
    public Ternary active;
}
