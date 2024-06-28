package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.mappers.rest.MapperMode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name =  "TwinBaseV2")
public class TwinBaseDTOv2 extends TwinBaseDTOv1 {
    @MapperModeBinding(modes = MapperMode.TwinStatusMode.class)
    @Schema(description = "status")
    public TwinStatusDTOv1 status;

    @MapperModeBinding(modes = MapperMode.TwinClassMode.class)
    @Schema(description = "class")
    public TwinClassDTOv1 twinClass;

    @MapperModeBinding(modes = MapperMode.TwinUserMode.class)
    @Schema(description = "current assigner")
    public UserDTOv1 assignerUser;

    @MapperModeBinding(modes = MapperMode.TwinUserMode.class)
    @Schema(description = "author")
    public UserDTOv1 authorUser;

    @MapperModeBinding(modes = MapperMode.TwinByHeadMode.class)
    @Schema(description = "headTwin")
    public TwinBaseDTOv2 headTwin;

    @MapperModeBinding(modes = MapperMode.TwinAliasMode.class)
    @Schema(description = "aliases")
    public List<TwinAliasDTOv1> aliases;
}
