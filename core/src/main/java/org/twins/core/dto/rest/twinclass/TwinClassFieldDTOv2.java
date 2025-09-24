package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.permission.PermissionDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinClassFieldV2")
public class TwinClassFieldDTOv2 extends TwinClassFieldDTOv1 {
    @Schema(description = "twin class")
    public TwinClassBaseDTOv1 twinClass;

    @Schema(description = "view permission")
    public PermissionDTOv1 viewPermission;

    @Schema(description = "edit permission")
    public PermissionDTOv1 editPermission;

    @Schema(description = "field typer featurer")
    public FeaturerDTOv1 fieldTyperFeaturer;

    @Schema(description = "rules bundles associated with this field")
    public List<TwinClassDependentFieldBundleDTOv1> conditionBundles;
}
