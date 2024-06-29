package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;
import org.twins.core.mappers.rest.MapperMode;

@Data
@Accessors(fluent = true)
@Schema(name = "TwinFieldV1")
public class TwinFieldDTOv1{
    @Schema(description = "On of values", example = "")
    public TwinFieldValueDTO value;

    @MapperModeBinding(modes = MapperMode.TwinClassFieldMode.class)
    @Schema(description = "class field details")
    public TwinClassFieldDTOv1 twinClassField;
}
