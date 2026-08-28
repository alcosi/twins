package org.twins.core.dto.rest.twinpointer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinPointerUpdateV1")
public class TwinPointerUpdateDTOv1 extends TwinPointerSaveDTOv1 {
    @Schema(description = "id", example = DTOExamples.UUID_ID)
    public UUID id;
}
