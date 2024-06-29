package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.mappers.rest.MapperMode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name =  "TwinV2")
public class TwinDTOv2 extends TwinBaseDTOv3 {
    @MapperModeBinding(modes = MapperMode.TwinFieldCollectionMode.class)
    @Schema(description = "fields")
    public Map<String, String> fields;
}
