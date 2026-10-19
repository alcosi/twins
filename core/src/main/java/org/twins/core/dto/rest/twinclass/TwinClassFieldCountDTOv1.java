package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "TwinClassFieldCountV1")
public class TwinClassFieldCountDTOv1 extends CountDTOv1 {
    @Schema(description = "required")
    public Boolean required;

    @Schema(description = "inheritable")
    public Boolean inheritable;

    @Schema(description = "system")
    public Boolean system;

    @Schema(description = "dependent field")
    public Boolean dependentField;

    @Schema(description = "has dependent fields")
    public Boolean hasDependentFields;

    @Schema(description = "projection field")
    public Boolean projectionField;

    @Schema(description = "has projected fields")
    public Boolean hasProjectedFields;

    @Schema(description = "twin class id", example = DTOExamples.UUID_ID)
    public UUID twinClassId;

    @Schema(description = "field typer featurer id")
    public Integer fieldTyperFeaturerId;

    @Schema(description = "twin sorter featurer id")
    public Integer twinSorterFeaturerId;

    @Schema(description = "field initializer featurer id")
    public Integer fieldInitializerFeaturerId;

    @Schema(description = "view permission id", example = DTOExamples.UUID_ID)
    public UUID viewPermissionId;

    @Schema(description = "edit permission id", example = DTOExamples.UUID_ID)
    public UUID editPermissionId;
}
