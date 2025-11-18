package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinFieldAttributeCreateV1")
public class TwinFieldAttributeCreateDTOv1 extends TwinFieldAttributeSaveDTOv1 {
    @Schema(description = "twin class field id")
    public UUID twinClassFieldId;
}
