package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "TwinV2")
public class TwinDTOv2 extends TwinBaseDTOv3 {
    @Deprecated
    @Schema(description = "old fields format")
    public Map<String, String> fields;

    @Schema(description = "fields")
    public Map<UUID, TwinFieldDTOv2> fieldsMap;
}
