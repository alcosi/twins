package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name =  "TwinV1")
public class TwinDTOv1 extends TwinBaseDTOv3 {
    @Schema(description = "fields")
    public List<TwinFieldDTOv1> fields;
}
