package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.enums.twinclass.OwnerType;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "TwinClassCountV1")
public class TwinClassCountDTOv1 extends CountDTOv1 {
    @Schema(description = "owner type")
    public OwnerType ownerType;

    @Schema(description = "is abstract")
    public Boolean abstractt;

    @Schema(description = "is segment")
    public Boolean segment;

    @Schema(description = "twin class freeze id", example = DTOExamples.UUID_ID)
    public UUID twinClassFreezeId;

    @Schema(description = "head twin class id", example = DTOExamples.UUID_ID)
    public UUID headTwinClassId;

    @Schema(description = "extends twin class id", example = DTOExamples.UUID_ID)
    public UUID extendsTwinClassId;

    @Schema(description = "marker data list id", example = DTOExamples.UUID_ID)
    public UUID markerDataListId;

    @Schema(description = "tag data list id", example = DTOExamples.UUID_ID)
    public UUID tagDataListId;

    @Schema(description = "twinflow schema space")
    public Boolean twinflowSchemaSpace;

    @Schema(description = "twin class schema space")
    public Boolean twinClassSchemaSpace;

    @Schema(description = "alias space")
    public Boolean aliasSpace;

    @Schema(description = "view permission id", example = DTOExamples.UUID_ID)
    public UUID viewPermissionId;

    @Schema(description = "head hunter featurer id")
    public Integer headHunterFeaturerId;

    @Schema(description = "edit permission id", example = DTOExamples.UUID_ID)
    public UUID editPermissionId;

    @Schema(description = "delete permission id", example = DTOExamples.UUID_ID)
    public UUID deletePermissionId;

    @Schema(description = "assignee required")
    public Boolean assigneeRequired;

    @Schema(description = "unique name")
    public Boolean uniqueName;

    @Schema(description = "has dynamic markers")
    public Boolean hasDynamicMarkers;

    @Schema(description = "bread crumbs face id", example = DTOExamples.UUID_ID)
    public UUID breadCrumbsFaceId;

    @Schema(description = "page face id", example = DTOExamples.UUID_ID)
    public UUID pageFaceId;
}
